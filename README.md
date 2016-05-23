BPM Engine
==

Simple implementation of an BPM engine, designed to mimic Activiti's behaviour. Optimized for high performance (rather than for the strict following of the BPMN specification).

Main features
--
- lightweight BPM engine, designed to be almost a drop-in replacement for the Activiti's engine;
- high-performance on-disk persistence;
- event scheduling (e.g. "timers");
- supports Activiti's XML format (both process and visual elements);
- process visualization;
- supports JUEL in flow expressions, task delegates, etc;
- JUnit support (and easy unit testing in general).

Supported elements:
--
- boundary event;
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
- in the current implementation of engine, "inclusive gateways" work exactly as "exclusive" - e.g. without the evaluation of flow expressions.
