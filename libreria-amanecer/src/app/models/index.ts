/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
export interface LoginRequest {
  userName: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userName?: string;
  rol?: string;
}

export interface RegisterRequest {
  nombreCompleto: string;
  userName: string;
  password: string;
  rol: string;
}

export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  categoria: string;
  precio: number;
  stock: number;
  stockMinimo: number;
  estado: 'ACTIVO' | 'INACTIVO' | 'STOCK_BAJO';
  imagenUrl?: string;
}

export interface Cliente {
  id: number;
  codigo: string;
  nombreCompleto: string;
  dni: string;
  telefono: string;
  direccion: string;
  estado: 'ACTIVO' | 'INACTIVO';
}

export interface DetalleVenta {
  productoId: number;
  codigo: string;
  producto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Venta {
  id: number;
  numeroVenta: string;
  clienteId: number;
  clienteNombre: string;
  fecha: string;
  hora: string;
  metodoPago: 'EFECTIVO' | 'YAPE_PLIN' | 'YAPE' | 'PLIN' | 'TARJETA' | 'TRANSFERENCIA';
  subtotal: number;
  igv: number;
  total: number;
  estado: 'COMPLETADA' | 'CANCELADA' | 'PENDIENTE';
  detalles: DetalleVenta[];
}

export interface NuevaVenta {
  clienteId: number;
  metodoPago: string;
  detalles: { productoId: number; cantidad: number; precioUnitario: number }[];
}

export interface ItemCarrito {
  carritoItemId?: number; // ID devuelto por POST /carrito/agregar
  producto: Producto;
  cantidad: number;
}

export interface Servicio {
  id: number;
  tipoServicio: string;
  cliente: string;
  cantidad: number;
  precioUnitario: number;
  total: number;
  metodoPago: string;
  fecha: string;
  hora: string;
}

export interface MovimientoInventario {
  id: number;
  productoId: number;
  producto: string;
  tipoMovimiento: 'ENTRADA' | 'SALIDA' | 'AJUSTE';
  cantidad: number;
  notas: string;
  fecha: string;
  usuario: string;
}

export interface StockInventario {
  productoId: number;
  codigo: string;
  producto: string;
  stockActual: number;
  stockMinimo: number;
  estado: 'NORMAL' | 'BAJO_STOCK' | 'REPONER';
}

export interface Usuario {
  id: number;
  nombre: string;
  usuario: string;
  rol: string;
  estado: 'ACTIVO' | 'INACTIVO';
}

export interface ReporteData {
  totalVentas: number;
  totalServicios: number;
  ticketPromedio: number;
  productosTop: ProductoTop[];
  ventasPorDia: { dia: string; ventas: number; servicios: number }[];
}

export interface ProductoTop {
  posicion: number;
  producto: string;
  cantidadVendida: number;
  total: number;
}

export interface DashboardStats {
  ventasHoy: number;
  productosActivos: number;
  serviciosHoy: number;
  stockBajo: number;
}
