var mongoose = require('mongoose');
console.log("Inside Books");
module.exports = mongoose.model('Books' , new mongoose.Schema({
	enteredby: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'User' },
    author: { type: String, default: "" },
    title: { type: String, default: "" },
    callNumber: { type: String, default: "" },
    publisher: { type: String, default: "" },
    year: { type: String, default: "" },
    location: { type: String, default: "" },
    copies: { type: String, default: "" },
    status: { type: String, default: "" },
    keywords: { type: String, default: "" },
    image: { type: String, default: "" },
    modifiedAt: Date
    
}));