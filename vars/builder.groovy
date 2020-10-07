
#!/usr/bin/env groovy

/**
* Builder DSL classes and step(s)
* TODO:
*   - validate svn usage
*/

// For debugging, adjust the logging level here
// to affect global DSL class logging level
// [NONE, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL]
@Field LOG_LEVEL = 'INFO'


// DSL class annotation for doc parsing
@interface DslClass { }
@interface DslStep { }

/**
* Abstract class for all DSL blocks.
*/
abstract class DslBase implements Serializable {
    protected def script
    protected String name
    public Logger logger

    public DslBase(def script) {
        this.script = script
        this.logger = new Logger(script, script.LOG_LEVEL)
    }

    public DslBase(def script, String name) {
        this.script = script
        this.name = name
        this.logger = new Logger(script, script.LOG_LEVEL)
        this.logger.module = name
    }

    public String getName() {
        return this.name
    }

    public String setName(String n) {
        this.logger.module = n
        this.name = n
    }

    /**
    * Check to assert that the property is null (assinged once). Generally
    * called at the beginning of a DSL property method if
    * the implementer wants to ensure an exception is thrown
    * if the property was called twice.
    * @param  val        The value
    * @param  propString The property name
    * @param  message    Custom message to display, otherwise a default exception
    * @return            Throws an Exception if value is not null
    */
    protected def assertNull(def val, String propString, String message = null) {
        if (null != val) {
            if (!message) {
                throw new Exception("cannot define ${propString} twice.")
            }
            else {
                throw new Exception(message)
            }
        }
    }

    /**
    * Evaluates the DSL property values for correctness when
    * called, throwing an exception if invalid.
    */
    def abstract evaluate()

    /**
    * Clones the DSL. Yep.
    */
    def abstract clone()

} // class DslBase

@DslClass
class TaskSteps extends DslBase {
    public String description
    public List sh = []
    public List bat = []
    public List env = []
    public List creds = []
    public def archive
    public Map build
    public Closure closure
    public Map p4s
    public Map p4cl
    public def git
    public def svn
    public def checkout

    public TaskSteps(def script) {
        super(script)
    }

    def TaskSteps(def script, String name) {
        super(script, name)
    }

    @DslStep
    def archive(String pattern) {
        assertNull(this.archive, 'archive')
        logger.debug "archive: $pattern"
        this.archive = [artifacts: pattern]
    }

    @DslStep
    def archive(Map map) {
        assertNull(this.archive, 'archive')
        logger.debug "archive: $map"
        this.archive = map
    }

    @DslStep
    def bat(String cmd) {
        logger.debug "bat: $cmd"
        this.bat.add(cmd)
    }

    @DslStep
    def bat(List cmds) {
        logger.debug "bat: $cmds"
        this.bat = this.bat + cmds
    }

    @DslStep
    def bat(Map map) {
        logger.debug "bat: $map"
        this.bat.add(map)
    }

    @DslStep
    def build(Map build) {
        assertNull(this.build, 'build')
        logger.debug "build: $build"
        this.build = build
    }

    @DslStep
    def build(String build) {
        assertNull(this.build, 'build')
        logger.debug "build: $build"
        this.build = [job: build, parameters: []]
    }

    @DslStep
    def closure(Closure c) {
        assertNull(this.closure, 'closure')
        logger.debug "closure: closure"
        this.closure = c
        this.closure.delegate = this
        this.closure.owner = this
    }

    @DslStep
    def creds(Object...creds) {
        logger.debug "creds: $creds"
        this.creds = creds
    }

    @DslStep
    def checkout(Map checkout) {
        assertNull(this.checkout, 'checkout')
        logger.debug "checkout: ${checkout}"
        this.checkout = checkout
    }

    @DslStep
    def description(String description) {
        assertNull(this.description, 'description')
        logger.debug "description: ${description}"
        this.description = description
    }

    @DslStep
    def desc(String description) {
        assertNull(this.description, 'description')
        logger.debug "description: ${description}"
        this.description = description
    }

    @DslStep
    def env(Map env) {
        logger.debug "env: $env"
        def envList = script.utils.mapToEnv(env)
        this.env.addAll(envList)
    }

    @DslStep
    def env(String...env) {
        logger.debug "env: $env"
        this.env.addAll(env)
    }

    @DslStep
    def git(Object git) {
        assertNull(this.git, 'git')
        logger.debug "git: ${git}"
        this.git = git
    }

    @DslStep
    def p4s(Map map) {
        assertNull(this.p4s, 'p4s')
        logger.debug "p4s: $map"
        this.p4s = map
    }

    @DslStep
    def p4cl(Map map) {
        assertNull(this.p4cl, 'p4cl')
        logger.debug "p4cl: $map"
        this.p4cl = map
    }

    @DslStep
    def sh(String cmd) {
        logger.debug "sh: $cmd"
        this.sh.add([script: cmd])
    }

    @DslStep
    def sh(List cmds) {
        logger.debug "sh: $cmds"
        def map = [:]
        cmds.each { this.sh.add([script: it]) }
    }

    @DslStep
    def sh(Map map) {
        logger.debug "sh: $map"
        this.sh.add(map)
    }

    /**
    * Either prepends or assigns (if not null) the given steps
    * to this task's steps.
    */
    def combineTaskSteps(TaskSteps otherTask) {
        if (!otherTask) { return }

        this.sh = otherTask.sh + this.sh
        this.bat = otherTask.bat + this.bat
        this.env = otherTask.env + this.env
        this.creds = otherTask.creds + this.creds
        this.archive = this.archive ?: otherTask.archive
        this.build =  this.build ?: otherTask.build
        this.closure = this.closure ?: otherTask.closure?.clone()
        this.p4s = this.p4s ?: otherTask.p4s
        this.p4cl = this.p4cl ?: otherTask.p4cl
        this.git = this.git ?: otherTask.git
        this.svn = this.svn ?: otherTask.svn
        this.checkout = this.checkout ?: otherTask.checkout
    }

    /**
    * Returns a string of all the step values in this instance.
    */
    String getStr() {
        def str = ''
        def tabs = ''
        // (0..numTabs).each { tabs += '\t' }
        str += "${tabs}description: ${this.description}\n"
        str += "${tabs}sh: ${this.sh}\n"
        str += "${tabs}bat: ${this.bat}\n"
        str += "${tabs}env: ${this.env}\n"
        str += "${tabs}creds: ${this.creds}\n"
        str += "${tabs}archive: ${this.archive}\n"
        str += "${tabs}build: ${this.build}\n"
        // str += "${tabs}closure: ${this.closure}\n"
        str += "${tabs}p4s: ${this.p4s}\n"
        str += "${tabs}p4cl: ${this.p4cl}\n"
        str += "${tabs}git: ${this.git}\n"
        str += "${tabs}svn: ${this.svn}\n"
        str += "${tabs}checkout: ${this.checkout}\n"
        return str
    }

    /**
    * Clones the current steps into a new object.
    */
    TaskSteps clone() {
        TaskSteps clonedTask = new TaskSteps(this.script)
        clonedTask.sh = this.sh.clone()
        clonedTask.bat = this.bat.clone()
        clonedTask.env = this.env.clone()
        clonedTask.creds = this.creds.clone()
        clonedTask.archive = this.archive?.clone()
        clonedTask.build = this.build?.clone()
        clonedTask.closure = this.closure?.clone()
        clonedTask.p4s = this.p4s?.clone()
        clonedTask.p4cl = this.p4cl?.clone()
        clonedTask.git = this.git?.clone()
        clonedTask.svn = this.svn?.clone()
        clonedTask.checkout = this.checkout?.clone()
        return clonedTask
    }

    def evaluate() { }
}

class TaskDSL extends TaskSteps {

    public String node = 'master'
    public Map axes
    // Task lists
    public List posts = []
    public List tasks = []

    public final boolean isBuilderTask = false

    def TaskDSL(def script) {
        super(script)
    }

    def TaskDSL(def script, String name) {
        super(script, name)
    }

    def TaskDSL(def script, String name, boolean isBuilderTask) {
        super(script, name)
        this.isBuilderTask = isBuilderTask
    }

    @DslStep
    def axes(Map config) {
        assertNull(this.axes, 'axes')
        logger.debug "axes: $config"
        this.axes = config
    }

    @DslStep
    def name(String name) {
        assertNull(this.name, 'name')
        this.name = name
    }

    @DslStep
    def node(String node) {
        if ('master' != this.node) {
            assertNull(this.node, 'node')
        }
        logger.debug "node: $node"
        this.node = node
    }

    @DslStep
    def post(Closure body) {
        logger.debug 'post'
        def t = new TaskDSL(this.script)
        body.delegate = t
        body()
        this.posts.add(t)
    }

    @DslStep
    def post(String name, Closure body) {
        logger.debug "post: ${name}"
        def t = new TaskDSL(this.script, name)
        body.delegate = t
        body()
        this.posts.add(t)
    }

    @DslStep
    def task(Closure body) {
        logger.debug 'task'
        def t = new TaskDSL(this.script)
        body.delegate = t
        body()
        this.tasks.add(t)
    }

    @DslStep
    def task(String name, Closure body) {
        logger.debug "task: ${name}"
        def t = new TaskDSL(this.script, name)
        body.delegate = t
        body()
        this.tasks.add(t)
    }

    String getStr() {
        def str = '\n'
        def tabs = ''
        // (0..numTabs).each { tabs += '\t' }
        str += "${tabs}name: ${this.name}\n"
        str += "${tabs}axes: ${this.axes}\n"
        str += super.getStr()

        // Task lists
        str += "${tabs}posts: ${this.posts.size()}\n"
        str += "${tabs}tasks: ${this.tasks.size()}\n"
        str += "${tabs}share: ${null != this.share}\n"
        str += "${tabs}notify: ${!this.notify.isEmpty()}\n"
        return str
    }

    TaskDSL clone() {
        TaskDSL clonedTask = new TaskDSL(this.script, this.name)
        clonedTask.combineTaskSteps(super.clone())

        clonedTask.name = this.name
        clonedTask.axes = this.axes?.clone()
        clonedTask.node = this.node

        // Recursively clone child tasks. Science!
        this.tasks.each { clonedTask.tasks.add(it.clone()) }
        this.posts.each { clonedTask.posts.add(it.clone()) }
        this.notify.each { clonedTask.notify.add(it.clone()) }
        clonedTask.share = this.share?.clone()

        return clonedTask
    }

    def evaluate() {
        if (!this.name) {
            throw new Exception('Must provide a name for a task (excluding top level "builder" call).')
        }

        (this.tasks + this.posts).each { it.evaluate() }
    }
} // class TaskDSL

// builder(String name, Closure body) {
def call(String name, Closure body) {
    // TODO
}


// def builder(String name) {
def call(Closure body) {
    // TODO
}