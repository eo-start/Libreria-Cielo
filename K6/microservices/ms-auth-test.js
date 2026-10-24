/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8095';
const AUTH_USER = __ENV.AUTH_USER || 'admin';
const AUTH_PASSWORD = __ENV.AUTH_PASSWORD || '123456';

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
        'MS-AUTH | POST /auth/login responde 200 o 400 controlado': (r) =>
            [200, 400].includes(r.status),
        'MS-AUTH | POST /auth/login responde en menos de 2000 ms': (r) =>
            r.timings.duration < 2000,
    });

    if (res.status === 200) {
        return extraerToken(res);
    }

    return null;
}

export default function () {
    let token = null;

    group('MS-AUTH | Login', () => {
        token = login();
    });

    group('MS-AUTH | Validar token JWT', () => {
        if (token) {
            const res = http.post(`${BASE_URL}/auth/validate?token=${token}`, null, headers());

            check(res, {
                'MS-AUTH | POST /auth/validate responde 200': (r) => r.status === 200,
                'MS-AUTH | POST /auth/validate responde en menos de 2000 ms': (r) =>
                    r.timings.duration < 2000,
            });
        }
    });

    group('MS-AUTH | Obtener ID del usuario autenticado', () => {
        if (token) {
            const res = http.get(`${BASE_URL}/auth/userId`, headers(token));

            check(res, {
                'MS-AUTH | GET /auth/userId responde 200': (r) => r.status === 200,
                'MS-AUTH | GET /auth/userId responde en menos de 2000 ms': (r) =>
                    r.timings.duration < 2000,
            });
        }
    });

    group('MS-AUTH | Obtener nombre del usuario autenticado', () => {
        if (token) {
            const res = http.get(`${BASE_URL}/auth/userName`, headers(token));

            check(res, {
                'MS-AUTH | GET /auth/userName responde 200': (r) => r.status === 200,
                'MS-AUTH | GET /auth/userName responde en menos de 2000 ms': (r) =>
                    r.timings.duration < 2000,
            });
        }
    });

    group('MS-AUTH | Listar usuarios', () => {
        const res = http.get(`${BASE_URL}/usuario/listar`, token ? headers(token) : headers());

        check(res, {
            'MS-AUTH | GET /usuario/listar status válido': (r) =>
                [200, 401, 403].includes(r.status),
            'MS-AUTH | GET /usuario/listar responde en menos de 2000 ms': (r) =>
                r.timings.duration < 2000,
        });
    });

    sleep(1);
}