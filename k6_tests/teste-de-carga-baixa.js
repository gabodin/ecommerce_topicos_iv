import http from 'k6/http'
import { sleep } from "k6"
import { Trend, Rate, Counter } from "k6/metrics"
import { check, fail } from 'k6'

export let options = {
    stages: [
        { duration: '10m', target: 10 },
    ],
}

export let latency = new Trend('latency')
export let successRate = new Rate('success_rate')
export let failureCount = new Counter('failure_count')
export let repairTime = new Trend('repair_time')
export let uptime = new Trend('uptime')

const BASE_URL = "http://localhost:9090"

let lastFailureTime = null;
let lastSuccessTime = Date.now();

export default function() {
    let ft = __ENV.FT_ENABLED || false
    let product = 10
    let user = 1
    let res = http.get(`${BASE_URL}/buy?product=${product}&user=${user}&ft=${ft}`)

    latency.add(res.timings.duration)
    successRate.add(res.status < 399)

    let isSuccess = check(res, {
        'success': (r) => r.status === 200,
    })

    if (!isSuccess) {
        failureCount.add(1);

        if (lastFailureTime === null) {
            let operationalTime = Date.now() - lastSuccessTime;
            if (operationalTime > 0) {
                uptime.add(operationalTime);
            }
            lastFailureTime = Date.now();
        }
    } else {
        if (lastFailureTime !== null) {
            let repairDuration = Date.now() - lastFailureTime;
            
            if (repairDuration > 0) {
                repairTime.add(repairDuration);  
            }
            
            lastFailureTime = null; 
        }
        lastSuccessTime = Date.now();
    }

    sleep(1)
}

export function handleSummary(data) {
    let failureOccurrences = data.metrics['failure_count']
        ? data.metrics['failure_count'].values.count
        : 0;
    let avgUptime = data.metrics.uptime ? data.metrics.uptime.values.avg : 'N/A'
    let avgRepairTime = data.metrics.repair_time ? data.metrics.repair_time.values.avg : 'N/A'
    let reqCount = data.metrics.http_reqs ? data.metrics.http_reqs.values.count : 'N/A'
    let durationInSeconds = data.state.testRunDurationMs / 1000 
    let latency =  data.metrics.latency ? data.metrics.latency.values.avg : 'N/A'

    let mtbf = failureOccurrences > 0 ? avgUptime : 0
    let mttr = failureOccurrences > 0 ? avgRepairTime : 0
    let availability = failureOccurrences > 0 ? (mtbf/(mtbf+mttr)*100).toFixed(2) : 100
    let  throughput = reqCount / durationInSeconds

    console.log(`Total de falhas: ${failureOccurrences}`);
    console.log(`MTBF (Tempo Médio Entre Falhas): ${mtbf.toFixed(2)} ms`);
    console.log(`MTTR (Tempo Médio de Reparo): ${mttr.toFixed(2)} ms`);
    console.log('Disponibilidade (%):', availability, '%' )
    console.log('Vazão (RPS):', throughput.toFixed(2), ' requisições por segundo')
    console.log('Latência (s):', latency.toFixed(2)/1000, 's')
}