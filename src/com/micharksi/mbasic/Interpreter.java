package com.micharksi.mbasic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>,
        Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;


    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("clock", new MBasicCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });

        globals.define("print", new MBasicCallable() {

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.println(arguments.get(0));
                return null;
            }
        });
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            MBasic.runtimeError(error);
        }
    }


    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }


    private void execute(Stmt stmt) {
        stmt.accept(this);
    }


    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }


    void executeBlock(List<Stmt> statements,
                      Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        return null;
    }

    @Override
    public Void visitNamespaceStmt(Stmt.Namespace stmt) {
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        return null;
    }
}
