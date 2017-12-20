var mongoose = require('mongoose');
console.log("Inside Books");

module.exports = mongoose.model('BorrowBooks' , new mongoose.Schema({
    bookId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'Books' },
    patronId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'User' },
    borrowDate: { type: Date, required: true, default: Date.now},    
    returnDate: Date,
    dueDate: { type: Date, required: true, default: function(){
        var someDate = new Date();
        var numberOfDaysToAdd = 30;
        someDate.setDate(someDate.getDate() + numberOfDaysToAdd); 
        return someDate;
    } },
    fine: {type: Number,required:true, default: 0 },
    renew: {type: Number,required:true, default: 0 }
}));
