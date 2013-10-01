import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy

import static ch.qos.logback.classic.Level.INFO

def defaultEncoder = {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

appender("STDOUT", ConsoleAppender) {
    with defaultEncoder
}

def catalinaBase = System.getProperty("catalina.base")
println "CATALINA_BASE: ${catalinaBase}"
if (catalinaBase == null) {
    appender("ReportAppender", ConsoleAppender) {
        with defaultEncoder
    }
} else {
    appender("ReportAppender", RollingFileAppender) {
        file = "${catalinaBase}/logs/report.log"
        with defaultEncoder
        rollingPolicy(FixedWindowRollingPolicy) {
            fileNamePattern = "report.%i.log.zip"
            minIndex = 1
            maxIndex = 3
        }
        triggeringPolicy(SizeBasedTriggeringPolicy) {
            maxFileSize = "100KB"
        }
    }
}

logger("edu.jhuapl.openessence.controller.ReportController", INFO, ["ReportAppender", "STDOUT"])

root(INFO, ["STDOUT"])
