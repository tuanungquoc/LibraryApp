var mongoose = require('mongoose');

module.exports = mongoose.model('BookWaitingList' , new mongoose.Schema({
    bookId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'Books' },
    patronId: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'User' },
    waitedDate: { type: Date, required: true, default: Date.now}
}));
