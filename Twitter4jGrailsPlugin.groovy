import org.apache.log4j.Logger;
import org.twitter4j.grails.plugin.TwitterUserStreamFactoryBean;
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class Twitter4jGrailsPlugin {

	Logger log = Logger.getLogger(getClass())

	def version = "3.0.2"

	def grailsVersion = "1.3 > *"

	def author = "Soeren Berg Glasius, Arthur Neves"
	def authorEmail = "soeren@glasius.dk, arthurnn@gmail.com"
	def title = "Twitter4j for Grails"
	def description = 'Wraps the Twitter4j API by Groovy delegation (see http://www.twitter4j.org for API documentation and examples). Plugin version follows twitter4j version included.'

	def documentation = "http://grails.org/plugin/twitter4j"

	def license = "APACHE"

	def developers = [
			[name: "Soeren Berg Glasius", email: "soeren@glasius.dk"],
			[name: "Arthur Neves", email: "arthurnn@gmail.com"]
	]

    def issueManagement = [ system: "GIT", url: "https://github.com/sbglasius/grails-twitter4j/issues" ]

    def scm = [ url: "https://github.com/sbglasius/grails-twitter4j.git" ]

	def dependsOn = [:]
	def pluginExcludes = [
			"grails-app/domain/**",
			"grails-app/taglib/**",
			"grails-app/utilites/**",
			"grails-app/views/error.gsp",
			"grails-app/layouts/**",
			"**/test/**"
	]

	def doWithWebDescriptor = { xml -> }

	def doWithSpring = {
		def prop = application.config.twitter.userListenerClass
		if(prop) {
			def clazz = application.classLoader.loadClass(prop)
			log.debug "Register twitter user listenet[${clazz}]"
			twitterUserListener(clazz)
		} else {
			log.debug "There is no Tweeter User Listener to register."
		}

		twitterStream(TwitterUserStreamFactoryBean) {
			configuration = ConfigurationHolder.config.twitter.'default'
		}
	}

	def doWithDynamicMethods = { ctx -> }

	def doWithApplicationContext = { applicationContext ->
		scheduleStream(applicationContext)
	}

	def onShutdown = { event ->
		def ctx = event.ctx
		if(!ctx) {
			log.error("Application context not found. Cannot execute shutdown code.")
			return
		}
		def twitterStream = ctx.getBean("twitterStream")
		if(twitterStream) {
			twitterStream.shutdown()
		}
	}

	def onChange = { event -> }

	def onConfigChange = { event -> }

	def scheduleStream = { ctx ->
		if(ctx.containsBean('twitterUserListener')) {
			def twitterUserListener = ctx.getBean('twitterUserListener')
			def twitterStream = ctx.getBean('twitterStream')
			twitterStream.addListener(twitterUserListener);
			// user() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
			twitterStream.user();

			log.debug "Twitter User Stream Created.."
		}
	}
}
