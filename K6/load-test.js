/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8095';
const AUTH_USER = __ENV.AUTH_USER || 'admin';
const AUTH_PASSWORD = __ENV.AUTH_PASSWORD || '123456';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 10 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.10'],
        http_req_duration: ['p(95)<2500', 'p(99)<4000'],
        checks: ['rate>0.85'],
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
        'LOAD | Login status controlado': (r) => [200, 400].includes(r.status),
        'LOAD | Login responde en menos de 2500 ms': (r) => r.timings.duration < 2500,
    });

    if (res.status === 200) {
        return extraerToken(res);
    }

    return null;
}

export default function () {
    const token = login();
    const params = token ? headers(token) : headers();

    group('LOAD | MS-AUTH', () => {
        const res = http.get(`${BASE_URL}/usuario/listar`, params);

        check(res, {
            'LOAD | MS-AUTH status válido': (r) => [200, 401, 403].includes(r.status),
            'LOAD | MS-AUTH responde en menos de 2500 ms': (r) => r.timings.duration < 2500,
        });
    });

    group('LOAD | MS-BOOK', () => {
        const libros = http.get(`${BASE_URL}/libros`);
        const categorias = http.get(`${BASE_URL}/categoria`);
        const provedores = http.get(`${BASE_URL}/provedores`);

        check(libros, {
            'LOAD | GET /libros 200': (r) => r.status === 200,
            'LOAD | GET /libros menos de 2500 ms': (r) => r.timings.duration < 2500,
        });

        check(categorias, {
            'LOAD | GET /categoria 200': (r) => r.status === 200,
            'LOAD | GET /categoria menos de 2500 ms': (r) => r.timings.duration < 2500,
        });

        check(provedores, {
            'LOAD | GET /provedores 200': (r) => r.status === 200,
            'LOAD | GET /provedores menos de 2500 ms': (r) => r.timings.duration < 2500,
        });
    });

    group('LOAD | MS-CLIENTE', () => {
        const clientes = http.get(`${BASE_URL}/cliente`, params);
        const vendedores = http.get(`${BASE_URL}/vendedor`, params);

        check(clientes, {
            'LOAD | GET /cliente status válido': (r) => [200, 401, 403].includes(r.status),
            'LOAD | GET /cliente menos de 2500 ms': (r) => r.timings.duration < 2500,
        });

        check(vendedores, {
            'LOAD | GET /vendedor status válido': (r) => [200, 401, 403].includes(r.status),
            'LOAD | GET /vendedor menos de 2500 ms': (r) => r.timings.duration < 2500,
        });
    });

    group('LOAD | MS-VENTA', () => {
        const carrito = http.get(`${BASE_URL}/carrito/listar`, params);
        const ventas = http.get(`${BASE_URL}/venta/listar`, params);

        check(carrito, {
            'LOAD | GET /carrito/listar status válido': (r) => [200, 401, 403].includes(r.status),
            'LOAD | GET /carrito/listar menos de 2500 ms': (r) => r.timings.duration < 2500,
        });

        check(ventas, {
            'LOAD | GET /venta/listar status válido': (r) => [200, 401, 403].includes(r.status),
            'LOAD | GET /venta/listar menos de 2500 ms': (r) => r.timings.duration < 2500,
        });

        if (token) {
            const addCarrito = http.post(
                `${BASE_URL}/carrito/agregar`,
                JSON.stringify({ libroId: 62, cantidad: 1 }),
                params
            );
            check(addCarrito, {
                'LOAD | POST /carrito/agregar status válido': (r) => [200, 201, 400].includes(r.status),
                'LOAD | POST /carrito/agregar menos de 2500 ms': (r) => r.timings.duration < 2500,
            });

            const realizarVenta = http.post(
                `${BASE_URL}/venta/realizar?clienteNombre=load-test`,
                null,
                params
            );
            check(realizarVenta, {
                'LOAD | POST /venta/realizar status válido': (r) => [200, 201, 400].includes(r.status),
                'LOAD | POST /venta/realizar menos de 2500 ms': (r) => r.timings.duration < 2500,
            });
        }
    });

    sleep(1);
}