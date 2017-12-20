var mongoose = require('mongoose');

module.exports = mongoose.model('BookReservation' , new mongoose.Schema({
    patronId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'User' },
    bookId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'Books' },    
    dueDate: { type: Date, required: true, default: function(){
        var someDate = new Date();
        var numberOfDaysToAdd = 3;
        someDate.setDate(someDate.getDate() + numberOfDaysToAdd); 
        return someDate;
    } }
}));
