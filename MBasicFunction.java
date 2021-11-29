package com.micharksi.mbasic;

import java.util.List;

class MBasicFunction implements MBasicCallable {
    private final Stmt.Function declaration;

    private final Environment closure;
  
 
/* Functions MBasic-function < Functions closure-constructor
  MBasicFunction(Stmt.Function declaration) {
*/
/* Functions closure-constructor < Classes is-initializer-field
  MBasicFunction(Stmt.Function declaration, Environment closure) {
*/

    private final boolean isInitializer;

    MBasicFunction(Stmt.Function declaration, Environment closure,
                boolean isInitializer) {
        this.isInitializer = isInitializer;


        this.closure = closure;

        this.declaration = declaration;
    }


    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }


    @Override
    public int arity() {
        return declaration.params.size();
    }


    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
/* Functions function-call < Functions call-closure
    Environment environment = new Environment(interpreter.globals);
*/

        Environment environment = new Environment(closure);

        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme,
                    arguments.get(i));
        }

/* Functions function-call < Functions catch-return
    interpreter.executeBlock(declaration.body, environment);
*/

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {

            if (isInitializer) return closure.getAt(0, "this");


            return returnValue.value;
        }



        if (isInitializer) return closure.getAt(0, "this");

        return null;
    }

}
