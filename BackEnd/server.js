// =======================
// get the packages we need ============
// =======================
var express     = require('express');
var app         = express();
var bodyParser  = require('body-parser');
var morgan      = require('morgan');
var mongoose    = require('mongoose');

var jwt    = require('jsonwebtoken'); // used to create, sign, and verify tokens
var config = require('./config'); // get our config file
var User   = require('./app/models/user'); // get our mongoose model

var crypto = require('crypto');
var nodemailer = require('nodemailer');
var expressValidator = require('express-validator');
var Token = require('./app/models/token')
// =======================
// configuration =========
// =======================
var port = process.env.PORT || 8080; // used to create, sign, and verify tokens
mongoose.connect(config.database); // connect to database
app.set('superSecret', config.secret); // secret variable


app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(expressValidator());
//app.use(expressValidator);

// use morgan to log requests to the console
app.use(morgan('dev'));

// =======================
// routes ================
// =======================
// basic route

var apiRoutes = express.Router(); 

apiRoutes.use(function(req,res,next){
    var token = req.body.token || req.query.token || req.headers['x-access-token'];
    if(token){
        jwt.verify(token, app.get('superSecret'),function(err,decoded){
            if(err){
                return res.json({ success: false, msg: 'Failed to authenticate token.' });
            }else{
                req.decoded = decoded;
                next();
            }
        });
    }else{
        return res.status(403).send({ 
            success: false, 
            msg: 'No token provided.' 
        });    
    }

});

app.use('/api', apiRoutes);

app.get('/test', function(req, res) {
    res.send('Hello! The API is at http://localhost:' + port + '/api');
});

app.post('/register', function(req, res) {
    req.checkBody('email', 'Enter a valid email').isEmail();

    var errors = req.validationErrors();
    if (errors) {
            
            res.send(errors);
            return;
        } 
    //   if (errors) { return res.status(400).send(errors); }
    User.findOne({ email: req.body.email }, function (err, user) {

        // Make sure user doesn't already exist
        if (user) return res.status(400).send({ success: false.valueOf() , msg: 'Your email address is already associated with another account.' });

        // Create and save the user
        user = new User({ name: req.body.name, email: req.body.email, password: req.body.password });
        user.save(function (err) {
            if (err) { return res.status(500).send({ success: false.valueOf(),msg: err.message }); }

            // Create a verification token for this user
            var token = new Token({ _userId: user._id, token: crypto.randomBytes(16).toString('hex') });

            // Save the verification token
            token.save(function (err) {
                if (err) { return res.status(500).send({ success: false.valueOf(), msg: err.message }); }

                // Send the email
                var transporter = nodemailer.createTransport(
                    {
                    host: 'smtp.gmail.com',
                    port: 465,
                    secure: true,
                    auth: {
                        user: 'tuan.ung.quoc.sjsu@gmail.com',
                        pass: 'Grandmum123'
                }});
                var mailOptions = { from: 'no-reply@yourwebapplication.com', to: user.email, subject: 'Account Verification Token', text: 'Hello,\n\n' + 'Please verify your account by this token:'+ token.token + '.\n' };
                transporter.sendMail(mailOptions, function (err) {
                    if (err) { return res.status(500).send({ success: false.valueOf(),msg: err.message }); }
                    res.status(200).send({success: true.valueOf(), msg:'User has been created susccessfully'});
                });
            });
        });
    });
});

app.post('/confirm',function(req,res){
    req.checkBody('email', 'Email is not valid').isEmail();
 
    // Check for validation errors    
    var errors = req.validationErrors();
    if (errors) return res.status(400).send(errors);
 
    // Find a matching token
    Token.findOne({ token: req.body.token }, function (err, token) {
        if (!token) return res.status(400).send({ success:false.valueOf(),type: 'not-verified', msg: 'We were unable to find a valid token. Your token my have expired.' });
 
        // If we found a token, find a matching user
        User.findOne({ _id: token._userId }, function (err, user) {
            if (!user) return res.status(400).send({success:false.valueOf(), msg: 'We were unable to find a user for this token.' });
            if (user.isVerified) return res.status(400).send({ success:false.valueOf(),type: 'already-verified', msg: 'This user has already been verified.' });
 
            // Verify and save the user
            user.isVerified = true;
            user.save(function (err) {
                if (err) { return res.status(500).send({ success:false.valueOf(),msg: err.message }); }
                res.status(200).send({success:true.valueOf(), msg:"The account has been verified. Please log in."});
            });
        });
    });
});


app.post('/login',function(req,res){
    req.checkBody('email', 'Email is not valid').isEmail();
    // Check for validation error
    var errors = req.validationErrors();
    if (errors) return res.status(400).send(errors);
 
    User.findOne({ email: req.body.email }, function(err, user) {
        if (!user) return res.status(401).send({ success:false.valueOf(), msg: 'The email address ' + req.body.email + ' is not associated with any account. Double-check your email address and try again.'});
        if(req.body.password != user.password){
            return res.status(401).send({ success:false.valueOf(),msg: 'Invalid email or password' });
        }
        
        if (!user.isVerified){ 
            return res.status(401).send({ success:false.valueOf(),type: 'not-verified', msg: 'Your account has not been verified.' }); 
        }
 
        // Login successful, write token, and send back user
        //generate token 
        const payLoad = {
            name: user.name,
            email: user.email
        };
        var tokenHeader = jwt.sign(payLoad, app.get('superSecret'));

        //res.send({ token: "myToken", user: user.toJSON() });
        res.json({
            success: true,
            message: 'Enjoy your token!',
            token: tokenHeader,
            email: user.email,
            userID: user._id
        });
    });
});


app.post('/resendToken',function(req,res){
    req.checkBody('email', 'Email is not valid').isEmail();
   
    // Check for validation errors    
    var errors = req.validationErrors();
    if (errors) return res.status(400).send(errors);
 
    User.findOne({ email: req.body.email }, function (err, user) {
        if (!user) return res.status(400).send({ success:false.valueOf,msg: 'We were unable to find a user with that email.' });
        if (user.isVerified) return res.status(400).send({ success:false.valueOf , msg: 'This account has already been verified. Please log in.' });
 
        // Create a verification token, save it, and send email
        var token = new Token({ _userId: user._id, token: crypto.randomBytes(16).toString('hex') });
 
        // Save the token
        token.save(function (err) {
            if (err) { return res.status(500).send({ success:false.valueOf,msg: err.message }); }
 
            // Send the email
            
            var transporter = nodemailer.createTransport(
                {
                host: 'smtp.gmail.com',
                port: 465,
                secure: true,
                auth: {
                    user: 'tuan.ung.quoc.sjsu@gmail.com',
                    pass: 'Grandmum123'
            }});
            var mailOptions = { from: 'no-reply@yourwebapplication.com', to: user.email, subject: 'Account Verification Token', text: 'Hello,\n\n' + 'Please verify your account by this token:'+ token.token + '.\n' };
            transporter.sendMail(mailOptions, function (err) {
                if (err) { return res.status(500).send({ success: false.valueOf(),msg: err.message }); }
                res.status(200).send({success: true.valueOf(), msg:'A verification token has been sent to ' + user.email + "."});
            });
        });
 
    });
});


// API ROUTES -------------------
// we'll get to these in a second

// =======================
// start the server ======
// =======================
app.listen(port);
console.log('Magic happens at http://localhost:' + port);
