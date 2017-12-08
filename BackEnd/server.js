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
var Books   = require('./app/models/books'); // get our mongoose model
var BorrowBooks = require('./app/models/detailstransaction');
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

app.post('/api/borrow', function(req, res) {
        var userId = req.body.patronId;
        var bookList = req.body.books;
        
        if(bookList.length > 3){
            res.status(403).send({success:false.valueOf(), msg:"There are more than 3 books"});
        }
        
        //search to see if each book is borroed by patron
       var bookListId = [];
       var borrowBookList = [];
       for(var i = 0 ; i < bookList.length; i++){
            bookListId.push(bookList[i]["bookId"]);
            borrowBookList.push(new BorrowBooks({bookId:bookList[i]["bookId"], patronId:userId, returnDate: null}));
       }
       //checking how many book he has been borrow for today
       const now = new Date();
       const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
       BorrowBooks.find({patronId: userId}).where('returnDate').equals(null).where('borrowDate').gte(today).exec(function(err,trans2){
            if(trans2.length >= 3 || (trans2.length + bookListId.length) >3 ) {return res.status(500).send({success:false.valueOf(),msg:"You have borrowed more than 3 books in a day"});}
            BorrowBooks.find({patronId: userId}).where('returnDate').equals(null).exec(function(err,trans1){
                if(trans1.length >= 9 || (trans1.length + bookListId.length) > 9 ) {return res.status(500).send({success:false.valueOf(),msg:"You have been borroing more than 9 books"});}
                BorrowBooks.find({patronId: userId}).where('bookId').in(bookListId).where('returnDate').equals(null).exec(function(err, trans){
                if(trans.length > 0) {return res.status(500).send({ success: false.valueOf() , msg: 'Some books are already borrowed!.' });}  
                BorrowBooks.collection.insert(borrowBookList,function(err){
                    if(err) {return res.status(500).send({ success: false.valueOf(),msg: err.message }); }
                    res.send({success:true.valueOf(), msg:"Transaction successfully!"});
                });
            }); 
            });  
       });
});

app.put('/api/returns', function(req, res) {
    var userId = req.body.patronId;
    var bookList = req.body.books;
    
    if(bookList.length > 9){
        res.status(403).send({success:false.valueOf(), msg:"There are more than 9 books"});
    }
    
    //search to see if each book is borroed by patron
   var bookListId = [];
   var bookObjectIDList = [];
   for(var i = 0 ; i < bookList.length; i++){
        bookListId.push(bookList[i]["bookId"]);   
        bookObjectIDList.push(mongoose.Types.ObjectId(bookList[i]["bookId"]));
   }
   const now = new Date();
   const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
   User.findOne({_id: {$in: [mongoose.Types.ObjectId(userId)]}},function(err,tran){
        if(err) {return res.status(500).send({success:false.valueOf(),msg:"cannot find user"});}
        var email = tran.email;
        //Start updating
        BorrowBooks.update({patronId:userId,bookId: {$in: bookListId}}, {returnDate: today},{multi: true},function(err){
            if(err){return res.status(500).send({success:false.valueOf(),msg:"cannot return"});}
            
            Books.find({_id: {$in: bookObjectIDList}},function(err,trans1){
                if(err) {return res.status(500).send(err);}
                var transporter = nodemailer.createTransport(
                    {
                    host: 'smtp.gmail.com',
                    port: 465,
                    secure: true,
                    auth: {
                        user: 'tuan.ung.quoc.sjsu@gmail.com',
                        pass: 'Grandmum123'
                }});
                var bookNameList = "";
                for(var i = 0 ; i < trans1.length; i++){
                    bookNameList = bookNameList + trans1[i].title + "\n";
                }
                var mailOptions = { from: 'no-reply@yourwebapplication.com', to: email, subject: 'Books Returned Confirmation', text: 'Hello,\n\n' + 'Confirm that you have returns following books:\n'+ bookNameList };
                transporter.sendMail(mailOptions, function (err) {
                    if (err) { return res.status(500).send({ success: true.valueOf() ,msg: err.message }); }
                    res.status(200).send({success: true.valueOf(), msg:'Return susccessfully'});
                });
            });
    
            
            
            
       });
   });
   
});

app.get('/api/getBooksBorrowedBy/:id',function(req,res){
    console.log("getbooksborrowed");
    var patronId = req.params.id;
    
    BorrowBooks.find({patronId:patronId}).where('returnDate').equals(null).select('bookId').exec(function(err,trans){
        if(err) {return res.status(500).send(err);}
        var bookIdList = [];
        if(trans.length > 0 ){
            for(var i = 0 ; i < trans.length ; i++){
                bookIdList.push(mongoose.Types.ObjectId(trans[i].bookId.toString()));
            }
        }
        
        Books.find({_id: {$in: bookIdList}},function(err,trans1){
            if(err) {return res.status(500).send(err);}
            return res.send({books: trans1})
        });

        
    });
});


app.post('/register', function(req, res) {
    req.checkBody('email', 'Enter a valid email').isEmail();
    req.checkBody('studentID','Enter a valid student ID').isNumeric();
    var errors = req.validationErrors();
    if (errors) {

            res.status(422).send(errors);
            return;
        }
    //   if (errors) { return res.status(400).send(errors); }
    User.findOne({ email: req.body.email }, function (err, user) {

        // Make sure user doesn't already exist
        if (user) return res.status(400).send({ success: false.valueOf() , msg: 'Your email address is already associated with another account.' });
        //parsing domain
        var email = req.body.email;
        var domain = email.replace(/.*@/, "");
        console.log(domain);
        // Create and save the user
        var role = 'patron';
        if(domain == 'sjsu.edu')
            role = 'librarian';

        user = new User({ name: req.body.name, email: req.body.email, password: req.body.password ,studentID: req.body.studentID, role: role.valueOf(), });

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

app.post('/api/addbooks',
		function (req, res) {

	        console.log("addbooks");

			var author = req.body.author;
			var title = req.body.title;
			var callNumber = req.body.callNumber;
			var publisher = req.body.publisher;
			var year = req.body.year;
			var location = req.body.location;
			var copies = req.body.copies;
			var status = req.body.status;
			var keywords = req.body.keywords;
			var image = req.body.image;
			var enteredby = req.body.enteredby;

	        Books.findOne({ author: req.body.author ,title: req.body.title }, function (err, book) {

	            // Make sure user doesn't already exist
	            if (book){

	            	return res.status(400).send({ success: false.valueOf() , msg: 'The book already exist, try editing existing.' });
	            }else{

		            // Create and save the user
		            book = new Books({
		            	author: author,
						title: title,
						callNumber:callNumber,
						publisher:publisher,
						year: year,
						location: location,
						copies:copies,
						status: status,
						keywords: keywords,
						image: image,
						enteredby: enteredby
		            	});

		            book.save(function (err) {
		                if (err) { return res.status(500).send({ success: false.valueOf(),msg: 'Book not saved!' }); }
		                return res.status(200).send({ success: true.valueOf(),msg: 'Book saved!' });
		            });
	            }
	        });

});

app.get('/api/getbooks',
	function (req, res) {

        console.log("getbooks");
		var title = req.param("title");
		var libId = req.param("enteredBy");
		console.log(title +","+ libId);
        var payload = {};
        if(title !== undefined && libId === undefined){
            payload = {title:title};
        }else if(title != undefined && libId !== undefined){
            payload = {title:title, enteredby:libId}
        }

		Books.find(payload, function (err, docs) {

			if (err) { return res.status(404).send({ success: false.valueOf(),msg: 'Book not found!' }); }
			if(docs.length ==0){ return res.status(404).send({ success: false.valueOf(),msg: 'Book not found!' }); }
	        res.json({
                books:docs
            });
		});
});

app.post('/api/deletebooks', function (req, res) {
	console.log("deletebooks");
   	var book_id = req.body.bookId;

	//TO BE DONE
	//CHECK IF THERES ANYONE BORROWED THIS BOOK, IF YES, FAIL AND SEND APPROPRIATE COMMENTS
			// ELSE IF NOT, GO AHEAD

   	BorrowBooks.find({ bookId:book_id}, function (err, docs){
   		console.log(docs);
   		if (docs){
   			console.log("doc exist");
   			return res.status(200).send({ success: true.valueOf(),msg: 'Book is borrowed, cant delete!' });
   		}else{
			Books.remove({ _id:book_id}, function (err, docs) {
		        if (err) { return res.status(404).send({ success: false.valueOf(),msg: 'Book not found!' }); }
		        else{  return res.status(200).send({ success: true.valueOf(),msg: 'Book deleted!' });}
			});
   		}	
   	});
});   	

app.put('/api/updatebooks', function (req, res) {
	console.log("editbooks");
	var book = req.body;
	var id = null;
	console.log(book);
	Books.findOne({ author: req.body.oauthor ,title: req.body.otitle }, function (err, bookserver) {

        // Make sure original exist and not already modified by other user.
        if (bookserver){
        	id = bookserver._id;
        	console.log(id);
        	Books.findOne({ author: req.body.author ,title: req.body.title  }, function (err, bookexisting) {
        		console.log(bookexisting);

        		if (bookexisting ){
		        	if (!(id.equals(bookexisting._id))){
		        		console.log(bookexisting._id);
		        		return res.status(400).send({ success: false.valueOf(),msg: 'The book already exist, try editing existing.' });
	        		}
        		}
			        console.log(id);
				    Books.update({_id: id}, {
				    	"author": book.author,
						"title": book.title,
						"callNumber":book.callNumber,
						"publisher":book.publisher,
						"year": book.year,
						"location": book.location,
						"copies":book.copies,
						"status": book.status,
						"keywords": book.keywords,
						"image": book.image,
						"enteredby": book.enteredby
				 	}, function (err, docs){
				    		if (err) { return res.status(404).send({ success: false.valueOf(),msg: 'Book not updated!' }); }
					        return res.status(200).send({  success: true.valueOf(),msg: 'Book updated!' });
				    });
        		});
        }
        else{
        	return res.status(404).send({ success: false.valueOf(),msg: 'Original book not found, try again' });
        }
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
            userID: user._id,
            role: user.role
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
