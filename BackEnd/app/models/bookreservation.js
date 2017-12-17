var mongoose = require('mongoose');

module.exports = mongoose.model('BookReservation' , new mongoose.Schema({
    patronId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'User' },
    bookId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'Books' },    
    createdAt: { type: Date, required: true, default: Date.now, expires: 259200 }
}));
