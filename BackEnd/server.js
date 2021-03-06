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
            //check if there is any reservation
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
        });       

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

app.post('/api/borrowabook', function(req, res) {
        var userId = req.body.patronId;
        var book = req.body.book;
        var borrowBook = new BorrowBooks({patronId:userId, bookId:book.bookId, returnDate:null});
        const now = new Date();
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        //checking to see if it is borrowed
        BorrowBooks.findOne({bookId:book.bookId}).where('returnDate').equals(null).exec(function(err,foundBook){
            if(err){
                return res.status(500).send({ success: false.valueOf() , msg: 'Please try to check out this book again' });                                                                
            }
            if(foundBook){
                return res.status(400).send({ success: false.valueOf() , msg: 'This book is borrowed by someone else' });                                                                
            }
            BookReservation.findOne({bookId: book.bookId},function(err,reservation){
                if(reservation){
                    if(reservation.patronId.toString() != userId ){
                        //if it is not my reservation, then cancel check out
                        return res.status(400).send({ success: false.valueOf() , msg: "This book "+book.title +" is reserved by some body else" });
                    }
                    //remove myself out of reservation then checking out
                    BookReservation.remove({_id:reservation._id},function(err,resv){
                        if(err) {
                            return res.status(500).send({ success: false.valueOf() , msg: 'There is something wrong. Please check out again this book:'+book.title });                                
                        }
                            //start checking out
                        borrowBook.save(function(err){
                            if(err) {
                                return res.status(500).send({ success: false.valueOf() , msg: 'There is something wrong. Please check out again this book:'+book.title });
                                                                     
                            }
                            console.log(userId);

                            User.findOne({_id: userId},function(err,user){
                                if(err){
                                    console.log("there are some err");
                                }
                                if(user){
                                    console.log("get into here");
                                    var transporter = nodemailer.createTransport(
                                        {
                                        host: 'smtp.gmail.com',
                                        port: 465,
                                        secure: true,
                                        auth: {
                                            user: 'tuan.ung.quoc.sjsu@gmail.com',
                                            pass: 'Grandmum123'
                                    }});
                                    var mailOptions = { from: 'no-reply@yourwebapplication.com', to: user.email, subject: 'Book checkout', text: 'Hello,\n\n' + 'You are checking out succesfully!!\n' };
                                    transporter.sendMail(mailOptions,function(err){
                                        return res.send({ success: true.valueOf() , msg: 'Succeed to check out' });
                                    });
                                }
                            });
        
                            
                        });
                    });    
                }else{
                    BookWaitingList.findOne({bookId:book.bookId},function(err,bookWaitList){
                        if(err){
                            return res.status(500).send({ success: false.valueOf() , msg: 'There is something wrong. Please check out again this book:'+book.title });                                                                
                        }
                        if(bookWaitList){
                            return res.status(400).send({ success: false.valueOf() , msg: "This book "+book.title +" has a wait list" });                        
                        }
                        BorrowBooks.find({patronId: userId}).where('returnDate').equals(null).where('borrowDate').gte(today).exec(function(err,trans2){
                            if(trans2.length >= 3 ) {return res.status(400).send({success:false.valueOf(),msg:"You have borrowed more than 3 books in a day"});}
                            BorrowBooks.find({patronId: userId}).where('returnDate').equals(null).exec(function(err,trans1){
                                if(trans1.length >= 9 ) {return res.status(400).send({success:false.valueOf(),msg:"You have been borrowing more than 9 books"});}
                                BorrowBooks.findOne({patronId: userId}).where('bookId').equals(book.bookId).where('returnDate').equals(null).exec(function(err, trans){
                                    if(err) { return res.status(500).send({ success: false.valueOf() , msg: 'Please try to check out this book again' });}                                                           

                                    if(trans) {return res.status(400).send({ success: false.valueOf() , msg: 'You already borrowed ' + book.title +"." });}  
                                    //
                                    borrowBook.save(function(err){
                                        if(err) {
                                            return res.status(500).send({ success: false.valueOf() , msg: 'There is something wrong. Please check out again this book:'+book.title });                                        
                                        }
                                        User.findOne({_id: userId},function(err,user){
                                            if(err){
                                                console.log("there are some err");
                                            }
                                            if(user){
                                                console.log("get into here");
                                                var transporter = nodemailer.createTransport(
                                                    {
                                                    host: 'smtp.gmail.com',
                                                    port: 465,
                                                    secure: true,
                                                    auth: {
                                                        user: 'tuan.ung.quoc.sjsu@gmail.com',
                                                        pass: 'Grandmum123'
                                                }});
                                                var mailOptions = { from: 'no-reply@yourwebapplication.com', to: user.email, subject: 'Book checkout', text: 'Hello,\n\n' + 'You are checking out succesfully!!!\n' };
                                                transporter.sendMail(mailOptions,function(err){
                                                    return res.send({ success: true.valueOf() , msg: 'Succeed to check out' });
                                                });
                                            }
                                        });
                                    });
                                }); 
                            });  
                        });  
        
                    });
                }
            });
        });
        //Checking book reservation
      

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
            //might trigger another call back to process a transaction
            // can bt put in another thread for reservations
            reservationProcess(doingReservation,bookObjectIDList);
            //
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
    
    BorrowBooks.find({patronId:patronId}).where('returnDate').equals(null).sort([['bookId', -1]]).exec(function(err,trans){
        if(err) {return res.status(500).send(err);}
        var bookIdList = [];
        if(trans.length > 0 ){
            for(var i = 0 ; i < trans.length ; i++){
                bookIdList.push(mongoose.Types.ObjectId(trans[i].bookId.toString()));
            }
        }
        
        Books.find({_id: {$in: bookIdList}}).sort([['_id', -1]]).exec(function(err,trans1){
            if(err) {return res.status(500).send(err);}
            var convertedJSON = JSON.parse(JSON.stringify(trans1));
            for(var i = 0 ; i < trans1.length ; i++){
                var temp = trans[i];
                var temp2 = trans1[i];
                if(trans1[i]._id.toString() == trans[i].bookId.toString()){
                    convertedJSON[i].dueDate = trans[i].dueDate;
                }
            }
            return res.send({books: convertedJSON})
        });

        
    });
});

app.get('/api/getBooksReservedBy/:id',function(req,res){
    console.log("Book reservation");
    var patronId = req.params.id;
    
    BookReservation.find({patronId:patronId}).exec(function(err,trans){
        if(err) {return res.status(500).send(err);}
        var bookIdList = [];
        if(trans.length > 0 ){
            for(var i = 0 ; i < trans.length ; i++){
                bookIdList.push(mongoose.Types.ObjectId(trans[i].bookId.toString()));
            }
        }
        
        Books.find({_id: {$in: bookIdList}}).sort([['_id', -1]]).exec(function(err,trans1){
            if(err) {return res.status(500).send(err);}
            var convertedJSON = JSON.parse(JSON.stringify(trans1));
            for(var i = 0 ; i < trans1.length ; i++){
                var temp = trans[i];
                var temp2 = trans1[i];
                if(trans1[i]._id.toString() == trans[i].bookId.toString()){
                    convertedJSON[i].dueDate = trans[i].dueDate;
                }
            }
            return res.send({books: convertedJSON})
        });

        
    });
});

app.put('/api/renew/:id/:patronId',function(req,res){
    var bookId = req.params.id;
    var patronId = req.params.patronId;
    BorrowBooks.findOne({bookId:bookId, patronId:patronId}).where('returnDate').equals(null).exec(function(err,borrowBook){
        if(err) return res.status(500).send({ success: false.valueOf(), msg:"Please try to renew again"});
         if(borrowBook){
             if(borrowBook.renew >= 2)
                return res.status(400).send({ success: false.valueOf(), msg:"This book is already renewed twice!"});
            //checking if the current date + 5days > dueDate
            var someDate = new Date();
            var numberOfDaysToAdd = 5;
            someDate.setDate(someDate.getDate() + numberOfDaysToAdd); 
            var test2 = borrowBook.dueDate;
            var test = someDate.getTime() -  test2.getTime();
            if( someDate.getTime() -  test2.getTime() < 0 )
                return res.status(400).send({ success: false.valueOf(), msg:"This book is too early to renew!"});
            var updatedBook = borrowBook;
            updatedBook.dueDate = borrowBook.dueDate.setDate(borrowBook.dueDate.getDate() + 30);
            updatedBook.renew = borrowBook.renew + 1;
            BorrowBooks.update({_id:borrowBook._id},
                updatedBook,
                function(err,doc){
                    if(err) return res.status(500).send({ success: false.valueOf(), msg:"Please try to renew again"});
                    return res.status(200).send({ success: true.valueOf(), msg:updatedBook.dueDate });
            });
        }

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
            role: user.role,
            name: user.name
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


var doingReservation = function(data) {
        if(data.length > 0){
            var reservationList = [] ;
            var waitingList = [];
            var patronList = [];
            BookWaitingList.aggregate({"$match":{"bookId": {"$in" : data}}},{"$group":{_id:"$bookId",  patronId:{$first:'$patronId'},waitlistId:{$first:'$_id'} ,firstCome: { $min: "$waitedDate" }}}).exec(function(err,firstComes){
                 //insert multiple documents into BookReservation table
                for(var i = 0 ; i < firstComes.length ;i++){
                     reservationList.push(new BookReservation({bookId:firstComes[i]._id,patronId:firstComes[i].patronId}));
                     waitingList.push(firstComes[i].waitlistId);
                     patronList.push(firstComes[i].patronId);
                }
                BookReservation.collection.insert(reservationList,function(err){
                    if(err) {
                        console.log(err);
                    }else{
                        //Find all user
                        User.find({_id: {$in: patronList}},function(err,users){
                            if(err){
                                console.log("there are some err");
                            }
                            for(var j = 0 ; j < users.length ; j++){
                                //seding mail to each of them
                                var transporter = nodemailer.createTransport(
                                    {
                                    host: 'smtp.gmail.com',
                                    port: 465,
                                    secure: true,
                                    auth: {
                                        user: 'tuan.ung.quoc.sjsu@gmail.com',
                                        pass: 'Grandmum123'
                                }});
                                var mailOptions = { from: 'no-reply@yourwebapplication.com', to: users[j].email, subject: 'Book reservation', text: 'Hello,\n\n' + 'Some books are reserved for you!!!\n' };
                                transporter.sendMail(mailOptions);
                            }
    
                            BookWaitingList.remove({_id: {$in: waitingList}},function(err,docs){
                                console.log("Transaction successfully!");
                            });
                        });
                    }
                });
                 
            });
        }
};

  
var reservationProcess = function(callback, returnBookList) {
    callback( returnBookList); // I dont want to throw an error, so I pass null for the error argument
};


var dueDateProcess = function(delta){
    var futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + delta);
    BorrowBooks.find({"dueDate":{$gt: futureDate}}).where('returnDate').equals(null).exec(function(err,futureDueBooks){
        if(err){
            console.log(err);
            
        }else{
            var listPatronId = [];
            for(var i = 0 ; i<futureDueBooks.length ; i++){
                var timeDiff = futureDueBooks[i].dueDate.getTime() - futureDate.getTime();
                var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24)); 
                if(diffDays <= 5){
                    //email to remind
                    listPatronId.push(futureDueBooks[i].patronId);
                }
            }
            if(listPatronId.length > 0){
                User.find({_id: {$in: listPatronId}},function(err,users){
                    if(err){
                        console.log("there are some err");
                    }
                    for(var j = 0 ; j < users.length ; j++){
                        //seding mail to each of them
                        var transporter = nodemailer.createTransport(
                            {
                            host: 'smtp.gmail.com',
                            port: 465,
                            secure: true,
                            auth: {
                                user: 'tuan.ung.quoc.sjsu@gmail.com',
                                pass: 'Grandmum123'
                        }});
                        var mailOptions = { from: 'no-reply@yourwebapplication.com', to: users[j].email, subject: 'Book due', text: 'Hello,\n\n' + 'Some books are due soon!!!\n' };
                        transporter.sendMail(mailOptions);
                    }
                });
            }
        }
    });
};

var borrowedBookProcess = function(delta){
    //find all borrowed books overdue
    var futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + delta);

    BorrowBooks.find({"dueDate":{$lt: futureDate}}).where('returnDate').equals(null).exec(function(err,overDueBooks){
            //Get all patron id
            var listBorrowedBooks = [];
            for(var i = 0 ; i < overDueBooks.length ; i++){
                //calculate for each book how many days are overdue
                var timeDiff = futureDate.getTime() - overDueBooks[i].dueDate.getTime();
                var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24)); 
                listBorrowedBooks.push({id:overDueBooks[i]._id, fine: diffDays});
            }
            updateFine(listBorrowedBooks);
    });
};
var updateFine = function(listBorrowedBooks){
    if(listBorrowedBooks.length > 0){
        BorrowBooks.findOne({_id:listBorrowedBooks[listBorrowedBooks.length - 1].id}).exec(function(err,borrow){
            if(borrow){
                //get current balance
                BorrowBooks.update({_id:borrow._id},
                    {
                        "fine":listBorrowedBooks[listBorrowedBooks.length - 1].fine
				 	}
                ).exec(function(err,count){
                        //rempve the last element in the array
                        console.log(count);
                        listBorrowedBooks.pop()
                        updateFine(listBorrowedBooks);
                });
            }
        });
    }
}

var reservationProcessCronJon = function(delta){
    //find all reservations books that having more than 3 days
    var futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + delta);
    var bookIdList = [];
    BookReservation.find({"dueDate":{$lt: futureDate}}).exec(function(err,reservations){
        var reservationsId = [];
        if(reservations.length > 0){
            for(var i =0 ; i<reservations.length;i++){
                bookIdList.push(reservations[i].bookId);
                reservationsId.push(reservations[i]._id);
            }
        }
        //remove this list of reservation
        if(reservationsId.length > 0){
            BookReservation.remove({_id: {$in : reservationsId}},function(err,docs){
                if(err) console.log(err);
                else{
                    doingReservation(bookIdList);
                }
            });
        }   
    });
};

app.get('/api/cronjob/:delta',function(req,res){
    var delta = Number(req.params.delta);
    borrowedBookProcess(delta);
    reservationProcessCronJon(delta);
    dueDateProcess(delta);
    res.send({msg:'ok'});
});

app.get('/api/balance/:patronId',function(req,res){
    var patronId = req.params.patronId;
    //find all fine of all books that are not returned
    BorrowBooks.find({patronId:patronId}).where('returnDate').equals(null).exec(function(err,listBooks){
            if(err) {return res.status(500).send({success:false.valueOf(),msg:'Please try to view again!'});}
            var fines = 0;
            for(var i = 0 ; i < listBooks.length ; i++){
                fines = fines + listBooks[i].fine;
            }
            return res.status(200).send({success:true.valueOf(),msg:fines});

    });

});


// API ROUTES -------------------
// we'll get to these in a second

// =======================
// start the server ======
// =======================
app.listen(port);
console.log('Magic happens at http://localhost:' + port);
