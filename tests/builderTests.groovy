/**
* Validates a top level task (builder) call with most of
* the bells and whistles.
*/
def builderTests_builder_Only() {
    builder('win') {

        node    'win-large'
        axes    VARIANT:['debug', 'release'],
                ARCH:   ['x86_64', 'x86']

        // A comma separated list of credentials
        creds   string(credentialsId: 'test-secret', variable: 'TOKEN'),
                usernamePassword(credentialsId: 'test-userpwd', usernameVariable: 'UNAME', passwordVariable: 'PWD')

        env     POTATO: 'diced',
                CAKE: 'chocolate'

        git     url: this.repoUri,
                branch: 'master',
                credentialsId: 'github'

        sh      label:  'validate env',
                script: '''
                        if [ "$POTATO" != "diced" ]; then exit 1; fi;
                        if [ "$CAKE" != "chocolate" ]; then exit 1; fi;
                        '''

        sh      label:  'validate creds',
                script: 'if [ "$UNAME" != "test" ]; then exit 1; fi;'
    }
}

/**
 * Mega dev test. Does a wide sweep across all functionality
 * of the builder step.
 * Nested tasks and validating the propogation of:
 *  - share block top level
 *  - nested share blocks
 *  - env vars (both overloads)
 *  - credentials
 *  - tasks, posts, axes
 */
def builderTests_NestedTasks_SmokeAll() {

     builder {

        share {

            env     "GLOBAL1=globalvalue1",
                    "GLOBAL2=globalvalue2"
            creds   usernamePassword(credentialsId: 'test-userpwd', usernameVariable: 'UNAME', passwordVariable: 'PWD')
            sh      label: 'global shared step',
                    script: '''
                    date +"%H:%M:%S"
                    if [ "$GLOBAL1" != "globalvalue1" ]; then exit 1; fi;
                    if [ "$GLOBAL2" != "globalvalue2" ]; then exit 1; fi;
                    if [ "$UNAME" != "test" ]; then exit 1; fi;
                    '''
        }

        task('axestask') {

            env     'TOP1=topvalue1'
            node    'win-large'
            axes    VARIANT:  ['Debug', 'Release'],
                    ARCH:     ['x86']
            sh      label: 'eval axes',
                    script: 'if [ "$TOP1" != "topvalue1" ]; then exit 1; fi;'

            task ('kid') {

                node    'win-large'
                sh      label: 'eval kid', script: 'if [ "$TOP1" == "topvalue1" ]; then exit 1; fi;'
            }
        }

        task('parent') {

            node    'win-large'
            env     'PARENT=topvalue2'
            creds   string(variable: 'TOKEN',  credentialsId: 'test-secret')
            sh      label: 'env parent', script: '''
                    if [ "$PARENT" != "topvalue2" ]; then exit 1; fi;
                    if [ "$TOKEN" == "" ]; then exit 1; fi;
                    '''

            share {
                env     PARENTSHARE: 'sharingiscaring'
                sh      label: "validate 'parent' share",
                        script: 'if [ "$PARENTSHARE" != "sharingiscaring" ]; then exit 1; fi;'
            }

            task('child') {

                node    'win-large'
                env     "CHILD=childvalue1"
                axes    AXIS1: ['stuff'],
                        AXIS2: ['thing', 'other']
                sh      label: 'eval child', script: '''
                        if [ "$CHILD" != "childvalue1" ]; then exit 1; fi;
                        if [ "$PARENT" == "topvalue2" ]; then exit 1; fi;
                        '''
                share {
                    env     'CHILD=tomato'
                }

                task('baby!') {

                    node    'win-large'
                    env     "BABY=leafvalue1"
                    sh      label: 'eval baby', script: '''
                            if [ "$BABY" != "leafvalue1" ]; then exit 1; fi;
                            if [ "$PARENT" == "topvalue2" ]; then exit 1; fi;
                            if [ "$CHILD" != "tomato" ]; then exit 1; fi;
                            echo YOOO > out.log
                            '''
                    archive '*.log'
                }
            }

            post('parent-post') {

                node    'win-large'
                env     'POSTCHILD=waffles'
                sh      label: 'eval parent-post', script: 'if [ "$POSTCHILD" != "waffles" ]; then exit 1; fi;'
            }
        }

        post('post') {

            node  'win-large'
            env   'POST1=postvalue1'
            sh    'if [ "$POST1" != "postvalue1" ]; then exit 1; fi;'

            task('posttask1') {

                node    'win-large'
                env     'POSTTASK1=bacon'
                sh      label: 'eval parent-post', script: ' if [ "$POSTTASK1" != "bacon" ]; then exit 1; fi;'
            }

            task('posttask2') {

                node    'win-large'
                env     'POSTTASK2=eggs'
                sh      label: 'eval parent-post', script: ' if [ "$POSTTASK2" != "eggs" ]; then exit 1; fi;'
            }
        }
    }
}


String getRepoUri() {
    return 'https://github.com/tabeyti/test.git'
}