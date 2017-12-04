var mongoose =  require('mongoose');
var Schema = mongoose.Schema;

// set up a mongoose model and pass it using module.exports
module.exports = mongoose.model('User', new Schema({
  name: String,
  email: { type: String, unique: true },
  password: String,
  isVerified: { type: Boolean, default: false },
  roles: [{ type: 'String' }],
  passwordResetToken: String,
  passwordResetExpires: Date,
  checkedBook: [{ type: mongoose.Schema.Types.ObjectId, required: true, ref: 'User' }]
}));
