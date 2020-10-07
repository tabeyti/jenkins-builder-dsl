/**
 * Base for the DSL classes.
 */
abstract class DslBase implements Serializable {
  protected def script
  public static Boolean DEBUG_ENABLED = false

  /**
   * Base constructor.
   * @param  script [description]
   */
  public DslBase(def script) {
    this.script = script
  }

  /**
   * Wrapper around script's println. Used for debug output.
   * @param  message [description]
   */
  protected debug(String message) {
    if (DslBase.DEBUG_ENABLED) {
      this.script.println message
    }
  }

  /**
   * Check to assert that the property is null. Generally
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
      if (null == message) {
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
} // class DslBase
