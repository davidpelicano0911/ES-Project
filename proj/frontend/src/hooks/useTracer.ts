import { trace } from '@opentelemetry/api';

const useTracer = () => {
    return trace.getTracer('Marketing-Frontend');
};

export default useTracer;
