// #!/usr/bin/groovy
package org.tools

class Logger implements Serializable {
    enum Level {
        NONE(0),
        FATAL(1),
        ERROR(2),
        WARN(3),
        INFO(4),
        DEBUG(5),
        TRACE(6),
        ALL(7)

        private int value

        Level(int val) {
            this.value = val
        }
    }

    public String module
    private def script
    private Level level = Level.ERROR

    def Logger(def script, String level = Level.ERROR.toString()) {
        this.script = script
        this.level = level as Level
    }

    def trace(String message) {
        this.log(Level.TRACE, message)
    }

    def debug(String message) {
        this.log(Level.DEBUG, message)
    }

    def info(String message) {
        this.log(Level.INFO, message)
    }

    def warn(String message) {
        this.log(Level.WARN, message)
    }

    def error(String message) {
        this.log(Level.ERROR, message)
    }

    def fatal(String message) {
        this.log(Level.FATAL, message)
    }

    String getLevel() {
        return this.level.toString()
    }

    String setLevel(String level) {
        this.level = level as Level
    }

    private def log(Level level, String message) {
        if (level > this.level) { return }

        def line = "[$level]: "
        line += (this.module) ? "${this.module} - " : ''
        line += message
        this.script.println line
    }
}
