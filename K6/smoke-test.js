/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8095';
const AUTH_USER = __ENV.AUTH_USER || 'admin';
const AUTH_PASSWORD = __ENV.AUTH_PASSWORD || '123456';

export const options = {
    vus: 1,
    iterations: 1,
    thresholds: {
        http_req_failed: ['rate<0.10'],
        http_req_duration: ['p(95)<2000'],
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
        'SMOKE | Login status controlado': (r) => [200, 400].includes(r.status),
        'SMOKE | Login responde en menos de 2000 ms': (r) => r.timings.duration < 2000,
    });

    if (res.status === 200) {
        return extraerToken(res);
    }

    return null;
}

export default function () {
    const token = login();
    const params = token ? headers(token) : headers();

    group('SMOKE | MS-AUTH', () => {
        const res = http.get(`${BASE_URL}/usuario/listar`, params);

        check(res, {
            'SMOKE | MS-AUTH disponible': (r) => [200, 401, 403].includes(r.status),
            'SMOKE | MS-AUTH responde rápido': (r) => r.timings.duration < 2000,
        });
    });

    group('SMOKE | MS-BOOK', () => {
        const res = http.get(`${BASE_URL}/libros`);

        check(res, {
            'SMOKE | MS-BOOK disponible': (r) => r.status === 200,
            'SMOKE | MS-BOOK responde rápido': (r) => r.timings.duration < 2000,
        });
    });

    group('SMOKE | MS-CLIENTE', () => {
        const res = http.get(`${BASE_URL}/cliente`, params);

        check(res, {
            'SMOKE | MS-CLIENTE disponible': (r) => [200, 401, 403].includes(r.status),
            'SMOKE | MS-CLIENTE responde rápido': (r) => r.timings.duration < 2000,
        });
    });

    group('SMOKE | MS-VENTA', () => {
        const res = http.get(`${BASE_URL}/venta/listar`, params);

        check(res, {
            'SMOKE | MS-VENTA disponible': (r) => [200, 401, 403].includes(r.status),
            'SMOKE | MS-VENTA responde rápido': (r) => r.timings.duration < 2500,
        });
    });

    sleep(1);
}