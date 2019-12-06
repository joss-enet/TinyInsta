var m = require("mithril")

var Homepage = require("./ui/Homepage.js")
var Login = require("./ui/Login.js")
var Inscription = require("./ui/Inscription.js")
var Timeline = require("./ui/Timeline.js")
var Post = require("./ui/Post.js")
var Profile = require("./ui/Profile.js")


m.route(document.body, "/homepage", {
	"/homepage": Homepage,
	"/login": Login,
	"/inscription": Inscription,
	"/timeline": Timeline,
	"/post": Post,
	"/profile/:pseudo": Profile,
})