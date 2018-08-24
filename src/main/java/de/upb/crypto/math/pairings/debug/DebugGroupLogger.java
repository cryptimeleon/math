package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.interfaces.structures.GroupElementMixedExpression;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class DebugGroupLogger {
    protected static class LogEntry {
        private String callee;
        private String opname;
        private String caller;

        public LogEntry(String callee, String opname, String caller) {
            this.callee = callee;
            this.opname = opname;
            this.caller = caller;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LogEntry logEntry = (LogEntry) o;
            return Objects.equals(callee, logEntry.callee) &&
                    Objects.equals(opname, logEntry.opname) &&
                    Objects.equals(caller, logEntry.caller);
        }

        @Override
        public int hashCode() {
            return Objects.hash(callee, opname, caller);
        }
    }

    /**
     * Set of entries
     */
    protected static HashMap<LogEntry, AtomicLong> entries = new HashMap<>();

    private static final HashSet<String> classesIgnoredInStacktraces = new HashSet<>(Arrays.asList(
            Thread.class.getName(),
            DebugGroupLogger.class.getName(),
            DebugGroup.class.getName(),
            DebugGroupElement.class.getName(),
            DebugBilinearMap.class.getName(),
            HashIntoDebugGroup.class.getName(),
            PowProductExpression.class.getName(),
            GroupElementMixedExpression.class.getName(),
            PairingProductExpression.class.getName()
    ));

    private static final Pattern stacktraceFilterPattern = Pattern.compile("(java\\.|jdk\\.|org\\.junit|com\\.intellij).*");

    /**
     * Only looks into the first maxStacktraceLength entries of the stacktrace (see log())
     */
    private static int maxStacktraceLength = 25;

    /**
     * Logs an entry
     */
    public static void log(String callee, String opname) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String caller = null;

        for (int i = 0; i < stackTraceElements.length && i < maxStacktraceLength; i++) {
            if (stackTraceElements[i] == null)
                break;
            if (classesIgnoredInStacktraces.contains(stackTraceElements[i].getClassName()))
                continue;
            if (stacktraceFilterPattern.matcher(stackTraceElements[i].getClassName()).matches())
                continue;

            //Set appropriate caller name
            if (caller == null) {
                caller = stackTraceElements[i].getFileName() + "::" + stackTraceElements[i].getMethodName();
            } else {
                caller = caller + "<-" + stackTraceElements[i].getFileName() + "::" + stackTraceElements[i].getMethodName();
            }

            //Log the entry
            LogEntry entry = new LogEntry(callee, opname, caller);
            AtomicLong count = entries.get(entry);
            if (count == null) {
                synchronized (entries) {
                    count = entries.get(entry);
                    if (count == null) {
                        count = new AtomicLong();
                        entries.put(entry, count);
                    }
                }
            }
            count.incrementAndGet();
        }
    }

    public static void print() {
        entries.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().callee + "|" + e.getKey().opname + "|" + e.getKey().caller))
                .forEachOrdered(e -> System.out.println(e.getValue().longValue()
                        + "\t"
                        + e.getKey().callee
                        + "::" + e.getKey().opname
                        + "\t" + e.getKey().caller
                ));
    }

    public static void reset() {
        entries = new HashMap<>();
    }
}
