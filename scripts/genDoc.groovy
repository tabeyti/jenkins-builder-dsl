#!/usr/bin/env groovy

/**
* Utilizes Groovy 2.5.X for execution.
*/
import org.codehaus.groovy.groovydoc.GroovyClassDoc
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool
import org.codehaus.groovy.groovydoc.GroovyMethodDoc
import org.codehaus.groovy.groovydoc.GroovyRootDoc
import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo
import groovy.transform.SourceURI

ROOT = "${new File(getClass().protectionDomain.codeSource.location.path).parent}/.."

def isDslClass(def clazz) {
    def result = clazz.annotations().collect {
        if ('DslClass' == it.name) { return it }
    }
    return !result.isEmpty()
}

def isDslStep(def method) {
    def result = method.annotations().collect {
        if ('DslStep' == it.name) { return it }
    }
    return !result.isEmpty()
}


def gdt = new GroovyDocTool(["$ROOT/vars"] as String[])
gdt.add(['builder.groovy'])

GroovyRootDoc root = gdt.getRootDoc()
GroovyClassDoc[] classDocs = root.classes()

def markdown = []
root.classes().reverse().each { c ->

    if (!isDslClass(c)) { return }

    // We've found a DSL class
    markdown.add("## ${c.qualifiedTypeName()}")
    markdown.add("${c.commentText()}")
    c.methods().each { GroovyMethodDoc m ->
        if (!isDslStep(m)) { return }
        def parameters = m.parameters().collect { return "${it.typeName().replace('java.lang.', '')}" }
        markdown.add("#### ${m.name()}(`${parameters.join(', ')}`)")
        markdown.add("${m.commentText()}")
        markdown.add('')
    }
    markdown.add('-'*80)
}

new File("$ROOT/dsl.md").write(markdown.join('\n'))
new File("$ROOT/vars/builder.txt").write(markdown.join('\n'))
