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
var BookWaitingList = require('./app/models/waitinglist');
var BookReservation = require('./app/models/bookreservation');
var crypto = require('crypto');
var nodemailer = require('nodemailer');
var expressValidator = require('express-validator');
var Token = require('./app/models/token')
var waterfall = require('async-waterfall');
var async = require('async');   
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

app.post('/api/waitingList/onBook/:bookId/byPatron/:patronId/copies/:copy', function(req,res){
        var bookId = req.params.bookId;
        var patronId = req.params.patronId;
        var copies = req.params.copy;
        var bookWaitingList = new BookWaitingList({bookId:bookId, patronId:patronId}); 
        //search to see if this book is borrowed already by this patron but not returned
        BorrowBooks.findOne({bookId:bookId, patronId:patronId}).where('returnDate').equals(null).exec(function(err,borrowedBookByPatron){
            if(err) {return res.status(500).send(err);}
            if(borrowedBookByPatron){
                return res.status(400).send({ success: false.valueOf() , msg: 'You already borrowed it' });                    
                // bookWaitingList.save(function(err){
                //     if (err) { return res.status(500).send({ success: false.valueOf(),msg: err.message }); }
                //     res.status(200).send({success:true.valueOf(), msg: 'Added to waiting list successfully' });                        
                // });
            }
            BookWaitingList.findOne({bookId:bookId, patronId:patronId},function(err,bookWaitList){
                if(bookWaitList){
                    //you are already in wait list
                    return res.status(400).send({ success: false.valueOf() , msg: 'You are already in the waiting list' });                                                                                
                }
                //Search to see if the book is available
                // BorrowBooks.aggregate({"$match":{$and: [ {"bookId": mongoose.Types.ObjectId(bookId)},{"returnDate":null}]}},{"$group":{_id:"$bookId",count:{$sum:1}}},{$sort: {_id: -1}}).exec(function(err,countings){
                //     if(err) {return res.status(500).send(err);}
                //     if(countings.length == 1){
                //         if(copies > countings[0]["count"]){
                //             return res.status(400).send({ success: false.valueOf() , msg: 'This book has some copies available to borrow' });                                                                                
                //         }
                //     }                
                // //Search to see if you arealy in wait list
                // });
                bookWaitingList.save(function(err){
                    if (err) { return res.status(500).send({ success: false.valueOf(),msg: err.message }); }
                    return res.send({success:true.valueOf(), msg: 'Added to waiting list successfully' });                        
                });
            });
        



            // BorrowBooks.findOne({bookId:bookId}).where('returnDate').ne(null).exec(function(err,borrowedBook){
            //     if(err) {return res.status(500).send(err);}
            //     if(borrowedBookByPatron){
            //         return res.status(400).send({ success: false.valueOf() , msg: 'This book is available to borrow' });                                                        
            //     }

            //     //Search to see if you arealy in wait list
            //     BookWaitingList.findOne({bookId:bookId, patronId:patronId},function(err,bookWaitList){
            //         if(bookWaitList){
            //             //you are already in wait list
            //             return res.status(400).send({ success: false.valueOf() , msg: 'You are already in the waiting list' });                                                                                
            //         }
            //         bookWaitingList.save(function(err){
            //             if (err) { return res.status(500).send({ success: false.valueOf(),msg: err.message }); }
            //             return res.send({success:true.valueOf(), msg: 'Added to waiting list successfully' });                        
            //         });
            //     });
                
            // });          
            
        });
             
        //search to see if this book is alray in this patron's wait list
       

});

app.get('/api/getWaitingListOf/:patronId',function(req,res){
    var patronId = req.params.patronId;
    
    BookWaitingList.find({patronId:patronId},function(err,trans){
        if(err) {return res.status(500).send(err);}
        var bookIdList = [];
        if(trans.length > 0 ){
            for(var i = 0 ; i < trans.length ; i++){
                bookIdList.push(mongoose.Types.ObjectId(trans[i].bookId.toString()));
            }
        }
        Books.find({_id: {$in: bookIdList}},function(err,trans1){
            if(err) {return res.status(500).send(err);}
            return res.status(200).send({books: trans1})
        });
    });
});

app.post('/api/borrow', function(req, res) {
        var userId = req.body.patronId;
        var bookList = req.body.books;
        
        if(bookList.length > 3){
            res.status(403).send({success:false.valueOf(), msg:"There are more than 3 books"});
        }
        
        //search to see if each book is borroed by patron
       var bookListId = [];
       var bookIdList = [];
       var borrowBookList = [];
       var copies = []
       for(var i = 0 ; i < bookList.length; i++){
            bookListId.push(bookList[i]["bookId"]);
            bookIdList.push(mongoose.Types.ObjectId(bookList[i].bookId.toString()));            
            borrowBookList.push(new BorrowBooks({bookId:bookList[i]["bookId"], patronId:userId, returnDate: null}));
            copies.push(bookList[i]["copies"]);
            
       }

       //checking how many book he has been borrow for today
       const now = new Date();
       const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
       //checking to see if there is any one on the waiting list
       BookWaitingList.find({bookId: {$in: bookListId}},function(err,trans1){
            if(err) {return res.status(500).send(err);}
            if(trans1.length > 0) {return res.status(400).send({ success: false.valueOf() , msg: 'There are waiting list in these books' });} 
            //check to see if this book is reserved for someone
            BookReservation.find({bookId: {$in: bookListId}},function(err,reservations){
                if(err) {return res.status(500).send(err);}
                if(reservations > 0 ) {return res.status(400).send({ success: false.valueOf() , msg: 'These books are reserved!!' });} 
                
                BorrowBooks.aggregate({"$match":{$and: [ {"bookId": {"$in" : bookIdList}},{"returnDate":null}]}},{"$group":{_id:"$bookId",count:{$sum:1}}},{$sort: {_id: -1}}).exec(function(err,countings){
                    if(err) {return res.status(500).send(err);}
                    for(var i = 0 ; i < countings.length ; i++){
                        if(countings[i]["_id"] == bookListId[i]){
                            if(copies[i] == countings[i]["count"])
                                return res.status(400).send({ success: false.valueOf() , msg: 'There are no copies in some books' });
                        }
                    }
                    //If all books in the transaction still have copies, then checking to see if:
                    // how many books this patron borrows in one day
                    // how many books this patron borrows over all
                    // if any books in this transaction he already borrowed
                    BorrowBooks.find({patronId: userId}).where('returnDate').equals(null).where('borrowDate').gte(today).exec(function(err,trans2){
                        if(trans2.length >= 3 || (trans2.length + bookListId.length) >3 ) {return res.status(500).send({success:false.valueOf(),msg:"You have borrowed more than 3 books in a day"});}
                        BorrowBooks.find({patronId: userId}).where('returnDate').equals(null).exec(function(err,trans1){
                            if(trans1.length >= 9 || (trans1.length + bookListId.length) > 9 ) {return res.status(500).send({success:false.valueOf(),msg:"You have been borrowing more than 9 books"});}
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
            });
            // Look for number of copies available for each book
           
       });
});

app.put('/api/returns', function(req, res) {
    var userId = req.body.patronId;
    var bookList = req.body.books;
    var errMsg = "";
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
            errMsg = errMsg + "Books are return successfully. \n"
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
                    if (err) { return res.status(200).send({ success: true.valueOf() ,msg: errMsg + "Cannot send email to confirm" }); }
                    //remove the first person in waiting list and move it to reservation
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
    var errMsg = "";
    var errors = req.validationErrors();
    if (errors) {

            res.status(422).send({ success: false.valueOf(), msg:errors});
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
            errMsg = errMsg + "User is created successfully. \n";
            // Create a verification token for this user
            var token = new Token({ _userId: user._id, token: crypto.randomBytes(16).toString('hex') });

            // Save the verification token
            token.save(function (err) {
                if (err) { return res.status(500).send({ success: false.valueOf(), msg: errMsg + err.message }); }
                errMsg = errMsg + "Token is generated sucessfully. \n";
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
                    if (err) { return res.status(500).send({ success: false.valueOf(),msg: errMsg + "Unable to send email confirmation" }); }
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
    if (errors) {res.status(400).send({ success:false.valueOf(),msg:errors});}

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
                if(err) {return res.status(500).send({ success: false.valueOf() , msg: "Unable to add book" });}
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
            if(title == "")
                payload = {enteredby:libId};
            else
                payload = {title:title, enteredby:libId}
        }

		Books.find(payload).sort([['bookId', -1]]).exec(function (err, docs) {

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
    var errMsg = "";
   	BorrowBooks.find({ bookId:book_id}).where('returnDate').equals(null).exec( function (err, docs){
   		console.log(docs);
   		if (docs.length > 0){
   			console.log("doc exist");
   			return res.status(200).send({ success: true.valueOf(),msg: 'Book is borrowed, cant delete!' });
   		}else{
            //Remove all the waiting list for this book and reservation
            BookReservation.remove({bookId:book_id},function(err,waitingList){
                if (err) { return res.status(404).send({ success: false.valueOf(),msg: 'Book cannot be deleted' }); }
                errMsg = errMsg + "Able to remove its reservation list successfully. \n";
                BookWaitingList.remove({bookId:book_id},function(err,waitingList){
                    if (err) { return res.status(404).send({ success: false.valueOf(),msg: errMsg+ 'Book cannot be deleted' }); }
                    errMsg = errMsg + "Able to remove its waiting list successfylly. \n";
                    Books.remove({ _id:book_id}, function (err, docs) {
                        if (err) { return res.status(404).send({ success: false.valueOf(),msg: errMsg + 'Book not found!' }); }
                        else{  return res.status(200).send({ success: true.valueOf(),msg: 'Book deleted!' });}
                    });
                });
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
        if(err) {return res.status(500).send({ success: false.valueOf(),msg: "Cannot find the book" }); }
        // Make sure original exist and not already modified by other user.
        if (bookserver){
        	id = bookserver._id;
        	console.log(id);
        	Books.findOne({ author: req.body.author ,title: req.body.title  }, function (err, bookexisting) {
                if(err) {return res.status(500).send({ success: false.valueOf(),msg: err }); }                
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
    var errMsg = "";
    // Check for validation errors
    var errors = req.validationErrors();
    if (errors) return res.status(400).send({ success:false.valueOf(),msg:errors});

    User.findOne({ email: req.body.email }, function (err, user) {
        if (!user) return res.status(400).send({ success:false.valueOf(),msg: 'We were unable to find a user with that email.' });
        if (user.isVerified) return res.status(400).send({ success:false.valueOf() , msg: 'This account has already been verified. Please log in.' });

        // Create a verification token, save it, and send email
        var token = new Token({ _userId: user._id, token: crypto.randomBytes(16).toString('hex') });

        // Save the token
        token.save(function (err) {
            if (err) { return res.status(500).send({ success:false.valueOf(),msg: err.message }); }
            errMsg = "Account has been created successfully. \n";
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
                if (err) { 
                    errMsg = errMsg + "Unable to resend confirmation email";
                    return res.status(500).send({ success: false.valueOf(),msg: errMsg }); 
                }
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
