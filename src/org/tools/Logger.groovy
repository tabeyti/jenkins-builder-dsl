// #!/usr/bin/groovy
package org.tools


class Logger implements Serializable {

    public String module
    private def script
    private final Map levels = [
        NONE: 0,
        FATAL: 1,
        ERROR: 2,
        WARN: 3,
        INFO: 4,
        DEBUG: 5,
        TRACE: 6,
        ALL: 7
    ]
    private String level = 'ERROR'


    def Logger(def script, String level = 'ERROR') {
        this.script = script
        this.level = level
    }

    def trace(String message) {
        this.log('TRACE', message)
    }

    def debug(String message) {
        this.log('DEBUG', message)
    }

    def info(String message) {
        this.log('INFO', message)
    }

    def warn(String message) {
        this.log('WARN', message)
    }

    def error(String message) {
        this.log('ERROR', message)
    }

    def fatal(String message) {
        this.log('FATAL', message)
    }

    String getLevel() {
        return this.level
    }

    String setLevel(String level) {
        this.level = level
    }

    private def log(String level, String message) {
        if (this.levels[level] > this.levels[this.level]) { return }

        def line = "[$level]: "
        line += (this.module) ? "${this.module} - " : ''
        line += message
        this.script.println line
    }
}