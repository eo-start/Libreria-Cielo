/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8083';

export const options = {
    stages: [
        { duration: '10s', target: 1 },
        { duration: '20s', target: 5 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.10'],
        http_req_duration: ['p(95)<2000'],
        checks: ['rate>0.85'],
    },
};

export default function () {
    group('MS-CLIENTE | Listar clientes', () => {
        const res = http.get(`${BASE_URL}/cliente`);

        check(res, {
            'MS-CLIENTE | GET /cliente responde 200': (r) => r.status === 200,
            'MS-CLIENTE | GET /cliente responde en menos de 2000 ms': (r) =>
                r.timings.duration < 2000,
            'MS-CLIENTE | GET /cliente retorna cuerpo válido': (r) =>
                r.body !== null && r.body !== undefined,
        });
    });

    group('MS-CLIENTE | Buscar cliente por ID', () => {
        const clienteId = __ENV.CLIENTE_ID || '1';
        const res = http.get(`${BASE_URL}/cliente/${clienteId}`);

        check(res, {
            'MS-CLIENTE | GET /cliente/{id} status controlado': (r) =>
                [200, 404, 500].includes(r.status),
            'MS-CLIENTE | GET /cliente/{id} responde en menos de 2000 ms': (r) =>
                r.timings.duration < 2000,
        });
    });

    group('MS-CLIENTE | Listar vendedores', () => {
        const res = http.get(`${BASE_URL}/vendedor`);

        check(res, {
            'MS-CLIENTE | GET /vendedor responde 200': (r) => r.status === 200,
            'MS-CLIENTE | GET /vendedor responde en menos de 2000 ms': (r) =>
                r.timings.duration < 2000,
            'MS-CLIENTE | GET /vendedor retorna cuerpo válido': (r) =>
                r.body !== null && r.body !== undefined,
        });
    });

    sleep(1);
}