/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8095';
const AUTH_USER = __ENV.AUTH_USER || 'admin';
const AUTH_PASSWORD = __ENV.AUTH_PASSWORD || '123456';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 30 },
        { duration: '1m', target: 50 },
        { duration: '1m', target: 80 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.20'],
        http_req_duration: ['p(95)<5000', 'p(99)<8000'],
        checks: ['rate>0.75'],
    },
};

function headers(token = null) {
    const h = {
        'Content-Type': 'application/json',
    };

    if (token) {
        h.Authorization = `Bearer ${token}`;
    }

    return { headers: h };
}

function extraerToken(response) {
    try {
        const body = response.json();
        return body.token || body.accessToken || body.jwt || body.value || null;
    } catch (e) {
        return null;
    }
}

function login() {
    const payload = JSON.stringify({
        username: AUTH_USER,
        password: AUTH_PASSWORD,
    });

    const res = http.post(`${BASE_URL}/auth/login`, payload, headers());

    check(res, {
        'STRESS | Login status aceptable': (r) =>
            [200, 400, 429, 500, 503].includes(r.status),
        'STRESS | Login responde en menos de 8000 ms': (r) =>
            r.timings.duration < 8000,
    });

    if (res.status === 200) {
        return extraerToken(res);
    }

    return null;
}

export default function () {
    const token = login();
    const params = token ? headers(token) : headers();

    group('STRESS | MS-AUTH', () => {
        const res = http.get(`${BASE_URL}/usuario/listar`, params);

        check(res, {
            'STRESS | MS-AUTH status aceptable': (r) =>
                [200, 401, 403, 429, 500, 503].includes(r.status),
            'STRESS | MS-AUTH responde en menos de 8000 ms': (r) =>
                r.timings.duration < 8000,
        });
    });

    group('STRESS | MS-BOOK', () => {
        const libros = http.get(`${BASE_URL}/libros`);
        const categorias = http.get(`${BASE_URL}/categoria`);

        check(libros, {
            'STRESS | GET /libros status aceptable': (r) =>
                [200, 429, 500, 503].includes(r.status),
            'STRESS | GET /libros menos de 8000 ms': (r) =>
                r.timings.duration < 8000,
        });

        check(categorias, {
            'STRESS | GET /categoria status aceptable': (r) =>
                [200, 429, 500, 503].includes(r.status),
            'STRESS | GET /categoria menos de 8000 ms': (r) =>
                r.timings.duration < 8000,
        });
    });

    group('STRESS | MS-CLIENTE', () => {
        const clientes = http.get(`${BASE_URL}/cliente`, params);
        const vendedores = http.get(`${BASE_URL}/vendedor`, params);

        check(clientes, {
            'STRESS | GET /cliente status aceptable': (r) =>
                [200, 401, 403, 429, 500, 503].includes(r.status),
            'STRESS | GET /cliente menos de 8000 ms': (r) =>
                r.timings.duration < 8000,
        });

        check(vendedores, {
            'STRESS | GET /vendedor status aceptable': (r) =>
                [200, 401, 403, 429, 500, 503].includes(r.status),
            'STRESS | GET /vendedor menos de 8000 ms': (r) =>
                r.timings.duration < 8000,
        });
    });

    group('STRESS | MS-VENTA', () => {
        const carrito = http.get(`${BASE_URL}/carrito/listar`, params);
        const ventas = http.get(`${BASE_URL}/venta/listar`, params);

        check(carrito, {
            'STRESS | GET /carrito/listar status aceptable': (r) =>
                [200, 401, 403, 429, 500, 503].includes(r.status),
            'STRESS | GET /carrito/listar menos de 8000 ms': (r) =>
                r.timings.duration < 8000,
        });

        check(ventas, {
            'STRESS | GET /venta/listar status aceptable': (r) =>
                [200, 401, 403, 429, 500, 503].includes(r.status),
            'STRESS | GET /venta/listar menos de 8000 ms': (r) =>
                r.timings.duration < 8000,
        });

        if (token) {
            const addCarrito = http.post(
                `${BASE_URL}/carrito/agregar`,
                JSON.stringify({ libroId: 62, cantidad: 1 }),
                params
            );
            check(addCarrito, {
                'STRESS | POST /carrito/agregar status aceptable': (r) =>
                    [200, 201, 400, 429, 500, 503].includes(r.status),
                'STRESS | POST /carrito/agregar menos de 8000 ms': (r) =>
                    r.timings.duration < 8000,
            });
        }
    });

    sleep(1);
}