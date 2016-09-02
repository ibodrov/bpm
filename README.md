## Further development is done in the [takari/bpm](https://github.com/takari/bpm) repository.

[![Build Status](https://travis-ci.org/ibodrov/bpm.svg?branch=master)](https://travis-ci.org/ibodrov/bpm)
[![Coverage Status](https://coveralls.io/repos/github/ibodrov/bpm/badge.svg?branch=master)](https://coveralls.io/github/ibodrov/bpm?branch=master)

BPM Engine
==

A simple implementation of an BPM engine, designed to mimic Activiti's behaviour. Optimized for high performance (rather than for the strict following of the BPMN specification).

Main features
--
- an lightweight BPM engine, inspired by Activiti BPM engine;
- high-performance on-disk persistence;
- event scheduling (e.g. "timers");
- supports Activiti's XML format (both process and visual elements);
- supports JUEL in flow expressions, task delegates, etc;
- JUnit support (and easy unit testing in general).

Supported elements:
--
- boundary event (errors and timers);
- call activity;
- end event;
- event-based gateway;
- exclusive gateway;
- inclusive gateway;
- intermediate catch event;
- parallel gateway;
- sequence flow;
- service task;
- start event;
- subprocess.

Limitations
--
- in the current implementation of the engine, "inclusive gateways" work exactly as "exclusive" - e.g. without the evaluation of flow expressions.
- tasks with TimerBoundaryEvents executed in the separate thread inside an unbounded Executor
