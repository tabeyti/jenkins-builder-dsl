## TaskSteps
 The steps that run on a task like block (e.g. builder, task, post).
 Most steps allow only one entry per instance, excluding the following:
   * `sh` and `bat`
   * `creds`
   * `env`

 These steps are sharable between task blocks via 'share' block(s).
 > __NOTE:__ Steps that only allow one definition per block (e.g. git, svn, p4s, etc.)
 > if "shared" will override the inheriting task's steps.

#### archive(`String`)
 `archiveArtifacts` Pipeline step wrapper
 ```groovy
 archive   '*.log'
 ```
    

#### archive(`Map`)
 `archiveArtifacts` Pipeline step wrapper
 ```groovy
 archive   artifacts: '*.log', allowEmptyArchive: true
 ```
    

#### bat(`String`)
 `bat` Pipeline step wrapper.
 ```groovy
 bat   'echo HI'
 ```
    

#### bat(`List`)
 `bat` Pipeline step wrapper.
 A list of strings as commands.
 ```groovy
 bat   'echo HI > out.log',
       'type out.log'
 ```
    

#### bat(`Map`)
 `bat` Pipeline step wrapper.
 ```groovy
 bat   label: 'greetings step',
       script: 'echo HI'
 ```
    

#### build(`Map`)
 `build` Pipeline step wrapper.
 ```groovy
 build job: 'test', parameters: [...]
 ```
    

#### build(`String`)
 `build` Pipeline step wrapper.
 ```groovy
 build 'test'
 ```
    

#### checkout(`Map`)
 `checkout` Pipeline step wrapper.
 ```groovy
 checkout  $class: 'GitSCM', branches: [[name: 'master']], extensions: ...
 ```
    

#### closure(`Closure`)
 A closure to be executed from the context of this class.
 User can access the 'script' member to invoke script
 level pipeline steps and such.

 __NOTE:__ Currently not sharable between steps
 ```groovy
 closure {
     def content = script.readFile 'config.json'
     script.build job: 'test', parameters: [string(name: 'CONFIG', value: content)]
 }
 ```
    

#### creds(`Object`)
 `withCredentials` Pipelin step wrapper. Takes a list of credentials
 objects to be used in the task.
 ```groovy
 creds usernamePassword(credentialsId: 'someid', usernameVariable: 'UNAME', passwordVariable: 'PWD'),
       string(credentialsId: 'otherId', variable: 'TOKEN'),
       ...
 ```
    

#### desc(`String`)
 Shorthand for the `description` step.
    

#### description(`String`)
 Sets the `currentBuild.description` during task execution.
    

#### env(`Map`)
 `withEnv` Pipeline step wrapper.
 Map key/values are converted to 'KEY=VAL' strings.
 ```groovy
 env   SOME_VAR1: "$myval",
       SOME_VAR2: "$otherval"
       ...
 ```
    

#### env(`String`)
 `withEnv` Pipeline step wrapper.
 ```groovy
 env   "SOME_VAR1=$someval",
       "SOME_VAR2=$otherval",
       ...
 ```
    

#### git(`Object`)
 `git` Pipeline step wrapper.
 ```groovy
 git   url: 'https://github.com/ham/sammich.git',
       branch: 'pickles'
 ```
    

#### p4cl(`Map`)
 `p4util.changelist` shared lib step wrapper
 ```groovy
 p4cl  branch: 'master',
       view: '//mydepot/branches/$P4_DATA_BRANCH/... //$P4_WORKSPACE/branches/$P4_DATA_BRANCH/...'
 ```
    

#### p4s(`Map`)
 `p4util.sync` shared lib step wrapper.

 Token replacement of env vars on  the map passed to this call occures
 so the user can take advantage of using `$P4_DATA_BRANCH` and
 `$P4_WORKSPACE` within their view mappings.
 ```groovy
 p4s   branch: 'master'
       view: '//mydepot/branches/$P4_DATA_BRANCH/... //$P4_WORKSPACE/branches/$P4_DATA_BRANCH/...'
 ```
    

#### sh(`String`)
 `sh` Pipeline step wrapper.
 ```groovy
 sh    'echo HI'
 ```
    

#### sh(`List`)
 `sh` Pipeline step wrapper.
 A list of strings as commands.
 ```groovy
 sh    'echo HI > out.log',
       'cat out.log'
 ```
    

#### sh(`Map`)
 `sh` Pipeline step wrapper.
 ```groovy
 sh    label: 'greetings step', script: 'echo HI'
 ```
    

--------------------------------------------------------------------------------
## TaskDSL
 The `task` block which allows a user to define an environment, a targeted
 agent and stage name, and a body of work (steps) to run.

 The call to `builder` is a task block, along with any subsequent `task` or `post`
 blocks defined inside. This means `task` blocks can be defined in a nested fashion,
 giving the user the ability to create execution trees.
 > __NOTE:__  Nested parallel tasks will have name of the parent task prepended to it.
 > This is for visualizing the chain of task calls when viewing in Jenkins.
 
#### axes(`Map`)
 A map of string lists for creating combinations
 where individual tasks are spawned to run each combo
 in parallel.

 The key of each map entry will be an env var that can be
 used in shell steps. Each keys will contain that task's combination
 values when executing.
 ```groovy
 axes  ARCH:       ['x86', 'x86_64'],
       VARIANT:    ['debug', 'release', 'pofile']
 ```
 > Will spawn 6 parallel tasks
    

#### enable(`Boolean`)
 Flag to disable this task from execution.
 ```groovy
 enable    (params.BUILD_TYPE == 'RC')
 ```
    

#### name(`String`)
 The name of the task. Used in stage labels.
 ```groovy
 name      'win-build'
 ```
    

#### node(`String`)
 The target node label to run the task on.
 ```groovy
 node      'win&&vs2019'
 ```
    

#### notify(`Closure`)
 Notify block to be executed on end of steps execution.
 See NotifyDSL for step details.
 ```groovy
 notify {
   slackSend   "Job ${env.JOB_NAME} FINISHED"
 }
 ```
    

#### post(`Closure`)
 Tasks to be ran following parallel `task` execution (at the end).
 > __NOTE:__ Post blocks are executed in serial in the order defined.
 ```groovy
 post {
   ...
 }
 ```
    

#### post(`String, Closure`)
 Tasks to be ran following `task` execution (at the end).
 > __NOTE:__ Post blocks are executed in serial in the order defined.
 ```groovy
 post('publish') {
   ...
 }
 ```
    

#### share(`Closure`)
 Steps to be given to all child tasks (global).
 They are not used the current task block where they are defined.
 ```groovy
 share {
   git     url: "git@github.com:myorg/my-repo.git"
   creds   string(credentialsId: 'my-api-token', variable: 'TOKEN')
   sh      'pip install -r requirements.txt'
 }
 ```
    

#### show(`Boolean`)
 Flag indicating whether to use a stage block or not.
 ```groovy
 show  false
 ```
    

#### task(`Closure`)
 A task block to be executed in parallel with other tasks
 defined within the same scope.
 ```groovy
 task {
   ...
 }
 ```
    

#### task(`String, Closure`)
 A task block to be executed in parallel with other tasks
 defined within the same scope.
 ```groovy
 task('my-task-name') {
   ...
 }
 ```
    

--------------------------------------------------------------------------------
## NotifyDSL
 The `notify` block defined in a task used for reporting status.
 This is executed on completion of a task (for now).

#### addErrors(`Boolean`)
 Flag indicating whether to add the failure list to a notification.
 Default is `false`
 ```groovy
 addErrors true
 ```
    

#### on(`String`)
 Indicates on what build status to send the email notification
 ```groovy
 on    'FAILURE' // SUCCESS, FAILURE, ABORT, null (always)
 ```
    

#### slack(`Map`)
 Shorthand for `slackSend` step.
    

#### slack(`String`)
 Shorthand for `slackSend` step.
    

#### slackSend(`Map`)
 `slackSend` Pipeline step wrapper.
 ```groovy
 slackSend     color: '#00FF00',
               message: 'when in Rome!',
               channel: 'my-notification-channel'
 ```
    

#### slackSend(`String`)
 `slackSend` Pipeline step wrapper.
 ```groovy
 slackSend     'ren in Wome'
 ```
    

--------------------------------------------------------------------------------