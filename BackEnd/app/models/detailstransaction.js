var mongoose = require('mongoose');
console.log("Inside Books");

module.exports = mongoose.model('BorrowBooks' , new mongoose.Schema({
    bookId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'Books' },
    patronId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'User' },
    borrowDate: { type: Date, required: true, default: Date.now},    
    returnDate: Date,
    renew: {type: Number,required:true, default: 0 }
}));
