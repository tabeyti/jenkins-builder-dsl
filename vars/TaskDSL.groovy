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
