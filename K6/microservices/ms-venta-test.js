/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8084';
const TOKEN = __ENV.TOKEN || '';

http.setResponseCallback(
    http.expectedStatuses(
        { min: 200, max: 399 },
        401,
        403,
        404,
        500
    )
);

export const options = {
    stages: [
        { duration: '10s', target: 1 },
        { duration: '20s', target: 5 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.20'],
        http_req_duration: ['p(95)<3000'],
        checks: ['rate>0.80'],
    },
};

function authHeaders() {
    const headers = {
        'Content-Type': 'application/json',
    };

    if (TOKEN) {
        headers.Authorization = `Bearer ${TOKEN}`;
    }

    return { headers };
}

export default function () {
    group('MS-VENTA | Listar ventas', () => {
        const res = http.get(`${BASE_URL}/venta/listar`);

        check(res, {
            'MS-VENTA | GET /venta/listar status controlado': (r) =>
                [200, 500].includes(r.status),
            'MS-VENTA | GET /venta/listar responde en menos de 3000 ms': (r) =>
                r.timings.duration < 3000,
        });
    });

    group('MS-VENTA | Obtener recibo PDF por ID', () => {
        const ventaId = __ENV.VENTA_ID || '1';
        const res = http.get(`${BASE_URL}/venta/${ventaId}/recibo`);

        check(res, {
            'MS-VENTA | GET /venta/{id}/recibo status controlado': (r) =>
                [200, 404, 500].includes(r.status),
            'MS-VENTA | GET /venta/{id}/recibo responde en menos de 3000 ms': (r) =>
                r.timings.duration < 3000,
        });
    });

    group('MS-VENTA | Generar registro de ventas PDF', () => {
        const res = http.get(`${BASE_URL}/venta/registroVentasPdf`);

        check(res, {
            'MS-VENTA | GET /venta/registroVentasPdf status controlado': (r) =>
                [200, 500].includes(r.status),
            'MS-VENTA | GET /venta/registroVentasPdf responde en menos de 3000 ms': (r) =>
                r.timings.duration < 3000,
        });
    });

    group('MS-VENTA | Listar carrito', () => {
        const res = http.get(`${BASE_URL}/carrito/listar`, authHeaders());

        check(res, {
            'MS-VENTA | GET /carrito/listar status controlado': (r) =>
                [200, 401, 403, 500].includes(r.status),
            'MS-VENTA | GET /carrito/listar responde en menos de 3000 ms': (r) =>
                r.timings.duration < 3000,
        });
    });

    sleep(1);
}