# SkuggaHttps

## Create and start your server
First off all, you'll need to get your configuration file. You can do that using the HttpConfigProvider 
class that fetches the "http.properties" file in your resources.
``` java
PropertiesConfiguration config = HttpConfigProvider.get();
```

PropertiesConfiguration provides an easy interface to interacts with .properties files in your resource 
folder. Using this configuration you can easily create and start your server.
``` java
HttpServer server = new HttpServer(new CustomExchangeHandler());
server.start();
```

In this code we do a couple of things. First we create the server using our property "server.ip" and "server.port". 
Next we provide a HttpExchangeHandler to the server. This class is created by you and describes what id types you 
are using as well as how to transform object into json. It would look like this for a MongoDB database using the 
BSON type ObjectId. The IdentityRepository method returns null here meaning this API is 'auth-less'. Note that this will
be implemented later in the README.
``` java
public class CustomExchangeHandler extends AbstractHttpExchangeHandler<ObjectId> {
	@Override
	public HttpJsonHandler getJsonHandler() {
		return new MoshiJsonHandler();
	}
	
	@Override
	public <Q, T extends Identifiable> DataRepository<Q, ObjectId, T> getIdentityRepository() {
		return null;
	}

	@Override
	public ObjectId createID(String s) {
		return new ObjectId(s);
	}
}
```

## Add controllers
To add controllers, you'll need to first tell the server where you store your controller classes. This package can be 
specified in "http.properties" with the key "server.package.controller". You can then simply create a new class in
this package annotated with Controller and extending AbstractController.
``` java
@Controller("meta")
public class MetaController extends AbstractController {

	@Get
	@Access({AllAccess.class})
	public Response ping() {
		return Response.ok();
	}

	@Get
	@Access({AllAccess.class})
	public ServerInfoDTO info() {
		return new ServerInfoDTO();
	}

}
```
PS: This controller is included with all SkuggaHTTPS servers and can be disabled by setting 'server.controller.meta' 
to false.

There are multiple thing to cover here. Firstly how the server build the url. In your configuration, you can set the
property 'server.root' to start every url with a prefix. This prefix is followed by the value of the controller
annotation. So in this case with a root of /api/ the url would be '/api/meta'.

## Add filters
To add filters, you'll need to first tell the server where you store your filter classes. This package can be 
specified in "http.properties" with the key "server.package.filter".

## Add models
To add models, you'll need to first tell the server where you store your model classes. This package can be specified
 in "http.properties" with the key "server.package.model".

## Add roles
(TO BE DONE)

## Link your data
(TO BE DONE)
