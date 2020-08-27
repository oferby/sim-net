grammar Ztn;


@header {
    import java.util.*;

}

@members {
    HashMap<String,String> values;
}


/*
* Parser Rules
*/

zn  returns [Map<String,String> values]
        :
        ( configCommand { $values = $configCommand.values; }
        | showCommand { $values = $showCommand.values; }
        | findPathCommand { $values = $findPathCommand.values; }
        | newCommand { $values = $newCommand.values; }
        | delCommand { $values = $delCommand.values; }
        | connectCommand { $values = $connectCommand.values; }
        | disconnectCommand { $values = $disconnectCommand.values; }
        | allowCommand { $values = $allowCommand.values; }
        | denyCommand { $values = $denyCommand.values; }
        | demoCommand { $values = $demoCommand.values; }
        | clearCommand { $values = $clearCommand.values; }
        | setPolicyCommand { $values = $setPolicyCommand.values; }
        | addToGroupCommand { $values = $addToGroupCommand.values; }
        )
        ;
configCommand returns [Map<String,String> values]
        : ( config | setup ) (p = ip | (m=protocol) searchble )
        {
                $values = new HashMap<String,String>();
                $values.put("operator", "config");
                if ($p.text != null)
                    $values.put("configure", "ip");
                if ($m.text != null && $m.text.equals("mpls"))
                    $values.put("configure", "mpls-path");

        };


showCommand returns [Map<String,String> values]
        : show (a='all')? (e=extEntity)? (n=name)?
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "show");
                if ($n.text != null )
                    $values.put("name", $n.text);
                if ($e.text != null )
                    $values.put("entity", $e.text);
                if ($a.text != null )
                    $values.put("name", "all");
            }
        ;


newCommand returns [Map<String,String> values]
        : newOperator NEW? e=extEntity (n=name)? (d='default')?
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "create");
                if ($n.text != null )
                    $values.put("name", $n.text);
                $values.put("entity", $e.text);
                if ($d.text != null )
                    $values.put("default", "true");
            }
        ;


delCommand returns [Map<String,String> values]
        : delOperator (a='all' | ((e=extEntity)? n=name))
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "delete");
                if ($a.text != null )
                    $values.put("all", "true");
                else {
                    $values.put("name", $n.text);
                    if ($e.text != null)
                        $values.put("entity", $e.text);
                }

            }
        ;


findPathCommand returns [Map<String,String> values]
        : ( search | show ) searchble from f=name to t=name
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "findPath");
                $values.put("from", $f.text);
                $values.put("to", $t.text);
            }
        ;

connectCommand returns [Map<String,String> values]
        : connect entity? f=name (to | and | with )? entity? t=name
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "connect");
                $values.put("from", $f.text);
                $values.put("to", $t.text);
            }
        ;

disconnectCommand returns [Map<String,String> values]
        : disconnect f=name from? and? (t=name)?
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "disconnect");
                $values.put("from", $f.text);
                if ($t.text != null )
                    $values.put("to", $t.text);

            }
        ;

allowCommand returns [Map<String,String> values]
        : allow (a='all')? access (from entity? f=name)? (to? entity? t=name)?
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "allow");
                if ($f.text != null )
                    $values.put("from", $f.text);
                if ($t.text != null )
                    $values.put("to", $t.text);
                if ($a.text != null )
                    $values.put("all", "true");
            }
        ;

denyCommand returns [Map<String,String> values]
        : deny access from entity? f=name to entity? t=name
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "deny");
                $values.put("from", $f.text);
                $values.put("to", $t.text);
            }
        ;

clearCommand returns [Map<String,String> values]
        : CLEAR
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "clear");
            }
        ;

demoCommand returns [Map<String,String> values]
        : newOperator DEMO num
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "demo");
                $values.put("num", $num.text);
            }
        ;


setPolicyCommand returns [Map<String,String> values]
        : 'set'? policy pn=name r=rights access from f=name to t=name
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "setPolicy");
                $values.put("name", $pn.text);
                $values.put("rights", $r.text);
                $values.put("from", $f.text);
                $values.put("to", $t.text);
            }
        ;

addToGroupCommand returns [Map<String,String> values]
        : ADD n=name to group g=name
            {
                $values = new HashMap<String,String>();
                $values.put("operator", "addToGroup");
                $values.put("name", $n.text);
                $values.put("group", $g.text);
            }
        ;

operator    : show | newOperator | search ;
extEntity   : entity | policy | group;
from        : FROM;
to          : TO ;
and         : AND ;
with        : WITH;
num         : NUMBER;
show        : SHOW ;
newOperator : ( CREATE | ADD );
delOperator : DELETE ;
search      : SEARCH ;
connect     : CONNECT;
protocol    : PROTOCOL;
ip          : IP;
mpls        : MPLS;
disconnect  : DISCONNECT;
allow       : ALLOW;
deny        : DENY;
rights      : ALLOW | DENY ;
access      : ACCESS | TRAFFIC;
entity      : ENTITY ;
policy      : POLICY ;
group       : GROUP ;
name        : NAME ;
searchble   : SEARCHABLE ;
setup       : SETUP;
config      : CONFIG;


/*
* Lexer Rules
*/

fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;
fragment DIGIT      : [0-9]+ ;

FROM        : 'from' ;
FOR         : 'for' ;
TO          : 'to' ;
AND         : 'and' ;
WITH        : 'with';
ADD         : 'add';
NUMBER      : DIGIT;
ACCESS      : 'access';
TRAFFIC     : 'traffic';
NEW         : 'new';
POLICY      : ( 'policy' | 'policies' ) ;
GROUP       : 'group' 's'? ;

SHOW        : ( 'show' |  'get' ) ;
CLEAR       : 'clear' ;

SEARCH      : ( 'find' | 'search' ) ;
SEARCHABLE  : ( 'path' | 'traffic' ) ;

CONNECT     : ( 'connect' | 'attach' ) ;
DISCONNECT  : ( 'disconnect' | 'detach' ) ;

CREATE      : ( 'create' | 'build' | 'start' ) ;
DELETE      : ( 'delete' | 'remove' ) ;

ALLOW       : ( 'allow' | 'grant' | 'permit' ) ;
DENY        : ( 'deny' | 'revoke' ) ;

ENTITY      : ('router' | 'vm' | 'virtual machine' | 'ecs' | 'server'| 'switch' |  'firewall' | 'service' | 'application' | 'mpls switch') 's'? ;
PROTOCOL    : ('mpls' | 'ethernet' ) ;
IP          : 'ip';
MPLS        : 'mpls';

DEMO        : 'demo' ;
SETUP       : 'setup';
CONFIG      : ( 'config' | 'configure' );

NEWLINE     : ('\r'? '\n' | '\r')+ ;
NAME        : ( LOWERCASE | UPPERCASE | DIGIT | '_' | '-')+ ;
WORD        : ( LOWERCASE | UPPERCASE | '_' )+ ;
WHITESPACE  : ' ' -> skip ;