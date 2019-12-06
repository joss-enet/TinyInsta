var m = require("mithril");

var data = {
		
	user: sessionStorage.getItem("user"),
    posts: [],
    buttonValue: [],
    
    loadPosts: function() {
        return m.request({
        	method: "GET",
            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/timeline?pseudo="+data.user,
        	}).then(function(result) {
        		data.posts = result.items;
        		data.posts.map(row => 
        			data.initLikeButton(row.properties.pseudo, row.properties.id)
        		)
        	})
    },
    
    initLikeButton: function(poster, postID) {
    	return m.request({
	    		method: "GET",
	    	    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+data.user+"/likes/"+poster+"/"+postID,
	    		}).then(function(result) {
	    			if (result.key.name == "ok") {
	    				data.buttonValue[poster+postID] = "Unlike";
	    	    	} else {
	    	    		data.buttonValue[poster+postID] = "Like";
	    	    	}
	    		})
    }
 
}

module.exports = {
		
	oninit: data.loadPosts,
		
	view: function() {	
	
		return m("main", [
			m("div.top-bar", 
				m("form.search-bar", {
					onsubmit: function(e) {
						e.preventDefault()
						m.request({
							method: "GET",
				            url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+search,
						}).then(function(result) {
							if (result.key.name == search) {
						    	m.route.set("/profile/:pseudo", {pseudo: search} );
					    	} else {
						    	alert("Profil non trouvé");
					    	}
						})
					}
	            }, [
	            	m("input.input[type=text][placeholder=Rechercher...]", {
	                    oninput: function (e) {search = e.target.value}
	                }),
	                m("button.button[type=submit]", "Rechercher")
	            ]),
				m("button.button.post[type=button]", {
					onclick: function(e) {m.route.set("/post");}
				}, "Poster")
			), 
			m("div.content-wrapper", 
					m("h1", "Timeline"),
					m("table.timeline", 
							data.posts.map(row => 
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
											    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+row.properties.pseudo+"/posts/"+row.properties.id+"?liker="+data.user,
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
											    url: "https://tinyinsta-257216.appspot.com/_ah/api/tinyInstaAPI/v1/users/"+row.properties.pseudo+"/posts/"+row.properties.id+"?liker="+data.user,
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
									
									
								}, data.buttonValue[row.properties.pseudo+row.properties.id])
							)
						)
					)
			)
		])
	}
}