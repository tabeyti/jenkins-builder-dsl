#!/usr/bin/env groovy

// Uncomment for debugging
// @Library('builder')_
import org.tools.Logger
import java.time.*
import groovy.transform.Field

/**
* Builder DSL classes and step(s)
* TODO:
*   - validate svn usage
*/

// For debugging, adjust the logging level here
// to affect global DSL class logging level
// [NONE, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL]
@Field LOG_LEVEL = 'ERROR'


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
class NotifyDSL extends DslBase {
    public String on
    // public Map email
    public Map slackSend
    public Boolean addErrors = false

     def NotifyDSL(def script) {
        super(script)
    }

    @DslStep
    def addErrors(Boolean addErrors) {
        logger.debug "addErrors $addErrors"
        this.addErrors = addErrors
    }

    @DslStep
    def on(String on) {
        logger.debug "on $on"
        this.on = on
    }

    @DslStep
    def slackSend(Map map) {
        assertNull(this.slackSend, 'slackSend')
        logger.debug "slackSend: $map"
        this.slackSend = map
    }

    @DslStep
    def slackSend(String message) {
        assertNull(this.slackSend, 'slackSend')
        logger.debug "slackSend: $message"
        this.slackSend = [message: message]
    }

    @DslStep
    def slack(Map map) {
        assertNull(this.slackSend, 'slack')
        logger.debug "slackSend: $map"
        this.slackSend = map
    }

    @DslStep
    def slack(String message) {
        assertNull(this.slackSend, 'slackSend')
        logger.debug "slackSend: $message"
        this.slackSend = [message: message]
    }

    String getStr() {
        def str = '\n'
        def tabs = ''
        // (0..numTabs).each { tabs += '\t' }
        str += "${tabs}slackSend: ${this.slackSend}\n"
        return str
    }

    NotifyDSL clone() {
        def clonedNotify = new NotifyDSL(this.script)
        clonedNotify.slackSend = this.slackSend?.clone()
        return clonedNotify
    }

    def evaluate() {
        if (null == slackSend) { // && null == email) {
            throw new Exception('Must provide at least one notification for the notify block.')
        }
    }
}

@DslClass
class TaskSteps extends DslBase {
    public String description
    public List sh = []
    public List bat = []
    public List env = []
    public List creds = []
    public def archive
    public Map build
    public Closure closureBlock
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
        assertNull(this.closureBlock, 'closure')
        logger.debug "closure: closure"
        this.closureBlock = c
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
        def envList = this.script.util.mapToEnv(env)
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
        this.closureBlock = this.closureBlock ?: otherTask.closureBlock?.clone()
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
        // str += "${tabs}closureBlock: ${this.closureBlock}\n"
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
        clonedTask.closureBlock = this.closureBlock?.clone()
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
    public Boolean enable = true
    public Boolean show = true
    // Task lists
    public List posts = []
    public List tasks = []
    public List notify = []
    public TaskSteps share

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
    def enable(Boolean isEnabled) {
        logger.debug "enable: $isEnabled"
        this.enable = isEnabled
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
    def notify(Closure body) {
        logger.debug 'notify'
        def n = new NotifyDSL(this.script)
        body.delegate = n
        body()
        this.notify.add(n)
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
    def share(Closure body) {
        assertNull(this.share, 'share')
        logger.debug 'share'
        def s = new TaskSteps(this.script)
        body.delegate = s
        body()
        this.share = s
    }

    @DslStep
    def show(Boolean show) {
        logger.debug "show $show"
        this.show = show
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
        str += "${tabs}enable: ${this.enable}\n"
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
        clonedTask.enable = this.enable

        // Recursively clone child tasks. Science!
        this.tasks.each { clonedTask.tasks.add(it.clone()) }
        this.posts.each { clonedTask.posts.add(it.clone()) }
        this.notify.each { clonedTask.notify.add(it.clone()) }
        clonedTask.share = this.share?.clone()

        return clonedTask
    }

    /**
    * Runs the current task as a block of work (checkout, commands, archiving)
    * on a targeted node under a specified stage.
    */
    def run() {
        logger.trace("time: ${LocalDateTime.now()}")

        // TODO: HACK: https://issues.jenkins-ci.org/browse/JENKINS-9104
        def envList = ["_MSPDBSRV_ENDPOINT_=${this.script.BUILD_TAG}"] + this.env
        envList += [
            // We add certain fields as env vars for shell/batch
            "name=${this.name}",
            "node=${this.node}",
        ]

        if (this.description) {
            this.script.currentBuild.description = this.description
        }

        this.script.stage(this.name, true) {
            this.script.node(this.node) {
                this.script.withEnv(envList) {
                    this.script.withCredentials(this.creds) {
                        def gitEnv = []

                        // Source checkout
                        if (this.git) {
                            logger.trace('git checkout')
                            def gitEnvMap = this.script.git this.git
                            gitEnv = this.script.util.mapToEnv(gitEnvMap)
                        }

                        if (this.svn) {
                            logger.trace('svn checkout')
                            this.script.svnCheckout(this.svn)
                        }

                        // Generic checkout step
                        if (this.checkout) {
                            this.script.checkout(this.checkout)
                        }

                        // Fallback to 'checkout scm' if no source control steps provided
                        if (!this.git && !this.svn && !this.checkout) {
                            logger.trace('scm checkout')
                            try { checkout scm } catch(e) {
                                logger.info('no scm found')
                            }
                        }

                        this.script.withEnv(gitEnv) {
                            this.sh.each { this.script.sh(it) }
                            this.bat.each { this.script.bat(it) }

                            if (this.closureBlock) {
                                logger.trace('found closure (awww :D )')
                                this.closureBlock()
                            }

                            if (this.build) {
                                this.script.build(this.build)
                            }
                        }
                    }

                    // Archive artifacts if specified
                    if (this.archive) {
                        logger.trace('archiving artifacts')
                        this.script.archiveArtifacts(this.archive)
                    }
                }
            }
        }
    }

    def evaluate() {
        if (!this.name) {
            throw new Exception('Must provide a name for a task (excluding top level "builder" call).')
        }

        (this.tasks + this.posts).each { it.evaluate() }
    }
} // class TaskDSL

/**
 * Wrapper around the general scm step for svn checkout
 * @param  remote The svn remote url
 */
def svnCheckout(String remote, String credId) {
    checkout poll: false,
        scm: [$class: 'SubversionSCM',
            additionalCredentials: [],
            excludedCommitMessages: '',
            excludedRegions: '',
            excludedRevprop: '',
            excludedUsers: '',
            filterChangelog: false,
            ignoreDirPropChanges: false,
            includedRegions: '',
            locations: [[credentialsId: 'TODO',
                depthOption: 'infinity',
                ignoreExternalsOption: true,
                local: '.',
                remote: remote]],
            quietOperation: true,
            workspaceUpdater: [$class: 'UpdateUpdater']
        ]
}

/**
 * Runs all 'notify' blocks defined in the task.
 * @param  failedTasksMap Map of stage-name to error-output failure entries.
 * @param  The task.
 */
def runNotify(Map failedTasksMap, TaskDSL task) {
    task.notify.each { notify ->
        def failedTasksStr = ''
        if (notify.addErrors) {
            failedTasksStr += '```\n'
            failedTasksMap.each { f ->
                failedTasksStr += "${f.key}: ${f.value}\n"
            }
            failedTasksStr += '```'
        }
        if (null != notify && (null == notify.on || currentBuild.currentResult == notify.on)) {
            task.logger.trace("notify")
            if (notify.slackSend) {
                notify.slackSend.message += "\n${failedTasksStr}"
                slackSend(notify.slackSend)
            }
        }
    }
}

/**
 * Stage step override to allow skipping a stage based on a flag.
 * @param  name             Stage name.
 * @param  execute          Bool indicating whether to run the stage.
 * @param  closure          The stage closure to execute.
 * @return                  A map of failures that occured, otherwise empty map.
 */
def stage(String name, Boolean execute, Closure closure) {
    return (execute) ? stage(name, closure) : { echo "skipped stage $name" }
}

/**
 * Recursive method for running the flow of task {...} block.
 * @param  task             The task object to run.
 * @param  sharedSteps      The shared task steps for this task and its children.
 * @return                  A map of failures that occured, otherwise empty map.
 */
Map runTask(TaskDSL task, TaskSteps sharedSteps = null) {
    task.logger.info("entered")
    task.logger.debug(task.str)
    task.logger.debug("shared steps: ${sharedSteps?.str}")

    def failedTasksMap = [:]
    def isEmptyTask = false
    def isBuilderTask = task.isBuilderTask

    //  this is the top level empty task, skip running
    if (!sharedSteps) {
        sharedSteps = new TaskSteps(this)
        isEmptyTask = true
    } else {
        // Run task with shared steps
        task.combineTaskSteps(sharedSteps)
        task.logger.debug("shared added: ${task.str}")
        task.logger.info("running")
        task.run()
    }

    task.logger.debug("children: ${task.tasks.size()}")

    // If this is a leaf task, then leaf!
    if (task.tasks.isEmpty() && task.posts.isEmpty()) { return failedTasksMap }

    // Before executing children (phrasing?), combine
    // the global shared steps with this parent task's shared steps
    // for its babies. Ordering of addition here is important as
    // we want the current task's shared steps to be invoked after
    // the globals.
    sharedSteps = sharedSteps.clone()
    sharedSteps.combineTaskSteps(task.share)

    task.logger.debug("combined shared steps: ${sharedSteps.str}")

    // If there is only one child task defined, with no axes, run it without
    // a parallel block so it doesn't looks weird in blue-ocean view
    if (1 >= task.tasks.size() && !task.tasks[0].axes) {
        def t = task.tasks[0]
        try {
            failedTasksMap += runTask(t, sharedSteps)
        } catch (e) {
            t.logger.error "Task failed!"
            catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') { throw new Exception(e) }
            failedTasksMap += ["${t.name}": e.message]
        } finally {
            runNotify(failedTasksMap, t)
            return failedTasksMap
        }
    }

    // Recursively run child tasks in parallel
    def parTasks = [:]
    task.tasks.each { t ->

        t.name = !isBuilderTask && !isEmptyTask ? "${task.name} ► ${t.name}" : t.name

        // If this task is disabled, GET OUT OF HERE!!
        if (!t.enable) { return }

        // If axes config was provided, create worker blocks from combinations of
        // the axes map's key/values and add them to the parallel task list
        if (t.axes) {
            def createComboList = util.createComboList(t.axes)
            createComboList.each { c ->
                // Add environment vars for the axes keys to be used in shell scripts.
                // Clone the task so we aren't concatenating env vars to the primary task.
                def clonedTask =    t.clone()
                clonedTask.env +=   util.mapToEnv(c)
                def envLabel =      util.mapToLabel(c)
                clonedTask.name =   "${clonedTask.name}-${envLabel}"

                task.logger.trace("cloned task: ${clonedTask.name}")
                task.logger.debug("cloned task: ${clonedTask.str}")

                parTasks[clonedTask.name] = {
                    try {
                        failedTasksMap += runTask(clonedTask, sharedSteps)
                    } catch (e) {
                        clonedTask.logger.error "Task failed!"
                        catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') { throw new Exception(e) }
                        failedTasksMap += ["${clonedTask.name}": e.message]
                    } finally {
                        runNotify(failedTasksMap, clonedTask)
                    }
                } // parTasks
            } // createComboList
        }
        // If a single task, add to parallel task list
        else {
            task.logger.trace("single task: ${t.name}")
            task.logger.debug("single task: ${t.name} - ${t.str}")
            parTasks[t.name] = {
                try {
                    failedTasksMap += runTask(t, sharedSteps)
                } catch (e) {
                    t.logger.error "Task failed!"
                    catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') { throw new Exception(e) }
                    failedTasksMap += ["${t.name}": e.message]
                } finally {
                    runNotify(failedTasksMap, t)
                }
            } // parTasks
        }
    }

    // Run all parallel tasks.
    try { parallel parTasks }
    catch (e) { /*ignore*/ }

    // Recursively run all 'post' tasks in serial.
    if (currentBuild.currentResult != 'FAILURE' && !task.posts.isEmpty()) {
        task.posts.each { p ->
            if (!p.enable) { return }
            p.name = !isBuilderTask ? "${task.name} ► ${p.name}" : p.name
            task.logger.trace("post task: ${p.name}")
            def envList = p.env
            try {
                failedTasksMap += runTask(p, sharedSteps)
            } catch (e) {
                task.logger.error "Task failed!"
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') { throw new Exception(e) }
                failedTasksMap += ["${p.name}": e.message]
            } finally {
                runNotify(failedTasksMap, p)
            }
        } // posts
    }

    return failedTasksMap
}

/**
 * Starts the execution of the top level 'Builder' task class.
 * @param  b        The builder task
 */
def runBuilder(TaskDSL b) {
    TaskDSL emptyParentTask = new TaskDSL(this)
    emptyParentTask.tasks.add(b)

    def failedTasksMap = runTask(emptyParentTask)

    if (failedTasksMap) {
        def message = ''
        failedTasksMap.each { message += "$it\n" }
        println "FAILED TASKS:\n$message"
    }
}

def call(Closure body) {
// def builder(Closure body) {
    TaskDSL b = new TaskDSL(this, 'builder', true)
    body.delegate = b
    body()
    b.evaluate()

    runBuilder(b)
}

def call(String name, Closure body) {
// def builder(String name, Closure body) {
    TaskDSL b = new TaskDSL(this, name, true)
    body.delegate = b
    body()
    b.evaluate()

    runBuilder(b)
}