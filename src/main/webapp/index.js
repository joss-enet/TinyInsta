var m = require("mithril")

var Homepage = require("./ui/Homepage.js")
var Login = require("./ui/Login.js")
var Inscription = require("./ui/Inscription.js")
var Timeline = require("./ui/Timeline.js")
var LoginFailed = require("./ui/LoginFailed.js")


m.route(document.body, "/homepage", {
	"/homepage": Homepage,
	"/login": Login,
	"/inscription": Inscription,
	"/timeline": Timeline,
	"/loginFailed": LoginFailed,
})