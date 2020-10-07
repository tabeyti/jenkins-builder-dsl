
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


/**
 * DSL 'task' object.
 * Holds properties specific to running a
 * body of work on an agent/node.
 *
 * This was created as a base class to be
 * used by (potentially) other custom DSL type
 * 'steps'.
 */
class TaskDSL extends DslBase {
  public String name
  public String node
  public String sh
  public String bat
  public List env = []
  public List creds = []
  public String archive
  public Map build
  public def git
  public String svn

  def TaskDSL(def script) {
    super(script)
  }

  def TaskDSL(def script, String name) {
    super(script)
    this.name = name
  }

  def name(String name) {
    assertNull(this.name, 'name')
    debug "name: $name"
    this.name = name
  }

  def node(String node) {
    assertNull(this.node, 'node')
    debug "node: $node"
    this.node = node
  }

  def git(Object git) {
    assertNull(this.git, 'git')
    debug "git: ${git}"
    this.git = git
  }

  def sh(String cmd) {
    assertNull(this.sh, 'sh')
    debug "${this.name} - sh: ${cmd}"
    this.sh = cmd
  }

  def sh(List cmds) {
    assertNull(this.sh, 'sh')
    debug "${this.name} - sh: ${cmds}"
    this.sh = cmds.join('\n')
  }

  def bat(String cmd) {
    assertNull(this.bat, 'bat')
    debug "${this.name} - bat: ${cmd}"
    this.bat = cmd
  }

  def bat(List cmds) {
    assertNull(this.bat, 'bat')
    debug "${this.name} - bat: ${cmds}"
    this.bat = cmds.join('\n')
  }

  def creds(Object...creds) {
    debug "creds: $creds"
    this.creds = creds
  }

  def env(String...env) {
    debug "env: $env"
    this.env.addAll(env)
  }

  def archive(String pattern) {
    assertNull(this.archive, 'archive')
    debug "archive: $pattern"
    this.archive = pattern
  }

  def evaluate() {
    if (null == this.node) {
      throw new Exception('Must provide a node for a task.')
    }
    if (null == this.name) {
      throw new Exception('Must provide a name for a task.')
    }
  }
} // class TaskDSL

def call(String name, Closure body) {
    // TODO
}

def call(Closure body) {
    // TODO
}