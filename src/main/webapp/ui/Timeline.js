var m = require("mithril");

var user = sessionStorage.getItem("user");

var data;
m.request({
	method: "GET",
    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/timeline?pseudo="+user,
	}).then(function(result) {
		data = result;
});

function initLikeButton(poster, postID) {
	m.request({
		method: "GET",
	    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+user+"/likes/"+poster+"/"+postID,
		}).then(function(result) {
			if (result.key.name == "ok") {
				return "Unlike";
	    	} else {
	    		return "Like";
	    	}
	});
};

module.exports = {
	
	view: function() {
	
		return m("main", [
			m("button.button.post[type=button]", {
				onclick: function(e) {m.route.set("/post");}
			}, "Poster"),
			m("div.content-wrapper", 
					m("h1", "Timeline"),
					m("h2", user),
					m("table.timeline", 
							data.items.map(row => 
							m("tr.timeline-pattern",
								m("p.postPseudo", row.properties.pseudo),
								m("img", {src: row.properties.image.value}),
								m("p.postMessage", row.properties.message),
								m("p.postLikes[id="+row.properties.pseudo+row.properties.id+"likes]", row.properties.likes),
								m("button.button[type=button][id="+row.properties.pseudo+row.properties.id+"button]",{
									onclick: function(e) {
										
										if (document.getElementById(row.properties.pseudo+row.properties.id+"button").textContent == "Like") {
											m.request({
												method: "PUT",
											    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+row.properties.pseudo+"/posts/"+row.properties.id+"?liker="+user,
												}).then(function(result) {
													if (result.key.name == "ok") {
														document.getElementById(row.properties.pseudo+row.properties.id+"button").textContent = "Unlike";
														document.getElementById(row.properties.pseudo+row.properties.id+"likes").textContent = parseInt(document.getElementById(row.properties.pseudo+row.properties.id+"likes").textContent)+1;
											    	} else {
											    		alert("Erreur.");
											    	}
											});
	
										} else {
											m.request({
												method: "DELETE",
											    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+row.properties.pseudo+"/posts/"+row.properties.id+"?liker="+user,
												}).then(function(result) {
													if (result.key.name == "ok") {
														document.getElementById(row.properties.pseudo+row.properties.id+"button").textContent = "Like";
														document.getElementById(row.properties.pseudo+row.properties.id+"likes").textContent = parseInt(document.getElementById(row.properties.pseudo+row.properties.id+"likes").textContent)-1;
											    	} else {
											    		alert("Erreur.");
											    	}
											});
										}
										
									}
									
									
								}, "Like")
							)
						)
					)
			)
		])
	}
}