/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8095';

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
    group('MS-BOOK | Listar libros', () => {
        const res = http.get(`${BASE_URL}/libros`);

        check(res, {
            'MS-BOOK | GET /libros responde 200': (r) => r.status === 200,
            'MS-BOOK | GET /libros responde en menos de 2000 ms': (r) =>
                r.timings.duration < 2000,
            'MS-BOOK | GET /libros retorna cuerpo': (r) => r.body && r.body.length > 0,
        });
    });

    group('MS-BOOK | Listar categorías', () => {
        const res = http.get(`${BASE_URL}/categoria`);

        check(res, {
            'MS-BOOK | GET /categoria responde 200': (r) => r.status === 200,
            'MS-BOOK | GET /categoria responde en menos de 2000 ms': (r) =>
                r.timings.duration < 2000,
        });
    });

    group('MS-BOOK | Listar proveedores', () => {
        const res = http.get(`${BASE_URL}/provedores`);

        check(res, {
            'MS-BOOK | GET /provedores responde 200': (r) => r.status === 200,
            'MS-BOOK | GET /provedores responde en menos de 2000 ms': (r) =>
                r.timings.duration < 2000,
        });
    });

    group('MS-BOOK | Filtrar libros por año', () => {
        const anio = __ENV.ANIO_LIBRO || '2024';
        const res = http.get(`${BASE_URL}/libros/anio/${anio}`);

        check(res, {
            'MS-BOOK | GET /libros/anio/{anio} status válido': (r) =>
                [200, 404].includes(r.status),
            'MS-BOOK | GET /libros/anio/{anio} responde en menos de 2000 ms': (r) =>
                r.timings.duration < 2000,
        });
    });

    group('MS-BOOK | Filtrar libros desde un año', () => {
        const anio = __ENV.ANIO_DESDE || '2020';
        const res = http.get(`${BASE_URL}/libros/anio/desde/${anio}`);

        check(res, {
            'MS-BOOK | GET /libros/anio/desde/{anio} status válido': (r) =>
                [200, 404].includes(r.status),
            'MS-BOOK | GET /libros/anio/desde/{anio} responde en menos de 2000 ms': (r) =>
                r.timings.duration < 2000,
        });
    });

    sleep(1);
}