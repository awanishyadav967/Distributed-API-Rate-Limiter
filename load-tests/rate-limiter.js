import http from 'k6/http';
import { sleep } from 'k6';
import { Counter } from 'k6/metrics';

const allowed = new Counter('allowed_requests');
const rejected = new Counter('rejected_requests');
const unexpected = new Counter('unexpected_responses');

export const options = {
    vus: 10,
    duration: '30s',
    thresholds: {
        unexpected_responses: ['count==0'],
    },
};

http.setResponseCallback(http.expectedStatuses(200, 429));

export default function () {
    const response = http.get(
        'http://host.docker.internal:8080/api/hello',
        { timeout: '5s' }
    );

    allowed.add(response.status === 200 ? 1 : 0);
    rejected.add(response.status === 429 ? 1 : 0);
    unexpected.add(
        response.status !== 200 && response.status !== 429 ? 1 : 0
    );

    sleep(0.1);
}