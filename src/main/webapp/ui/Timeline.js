var m = require("mithril");

var user = sessionStorage.getItem("user");

var data;
m.request({
	method: "GET",
    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/timeline?pseudo="+user,
	}).then(function(result) {
		data = result;
})


module.exports = {

	view: function() {
		return m("main", [
			m("button.button.post[type=button]", {
				onclick: function(e) {m.route.set("/post")}
			}, "Poster"),
			m("h1", "Timeline"),
			m("h2", user),
			m("table", 
				m("tr",
						m("th", "Auteur"),
						m("th", "Image"),
						m("th", "Message"),
						m("th", "Likes")
				),
				data.items.map(row => 
					m("tr.post", 
						m("td.postPseudo", row.properties.pseudo),
						m("td.postImage", 
								m("img", {src: row.properties.image.value})
						),
						m("td.postMessage", row.properties.message),
						m("td.postLikes", row.properties.likes)
					)	
				)
			)
		])
		
		
	}
}