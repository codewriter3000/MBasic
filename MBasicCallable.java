package com.micharksi.mbasic;

import java.util.List;

interface MBasicCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}