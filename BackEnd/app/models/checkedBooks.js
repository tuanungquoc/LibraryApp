var mongoose =  require('mongoose');
var Schema = mongoose.Schema;

// set up a mongoose model and pass it using module.exports
module.exports = mongoose.model('CheckedBook', new Schema({
  BookInfo: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'Books' },
  checkedOutDate: Date,
  dueDate: Date
}));
