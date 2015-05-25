BPM Engine
==

Simple implementation of BPM engine, designed to mimic Activiti's behaviour. Optimized for high performance (rather than for strict following of BPMN specification).

Main features
--
- lightweight BPM engine, designed to be almost a drop-in replacement for Activiti's engine;
- high-performance on-disk persistence;
- event scheduling (e.g. "timers");
- supports Activiti XML format (both process and visual elements);
- process visualization;
- supports JUEL in flow expressions, task delegates, etc;
- JUnit support.

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
- in the current implementation of engine, "inclusive gateway" works exactly as "exclusive" - i.e. without evaluating flow expressions.

TODO
--
- extract persistence and event scheduling into separate modules;
- support Camunda's BPMN editor;
- support more element types?
