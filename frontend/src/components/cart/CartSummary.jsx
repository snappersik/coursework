import React from 'react';

const CartSummary = ({ items, userId }) => {
    const total = items.reduce((sum, item) => sum + item.price * item.quantity, 0);
    const fee = 299;
    const discount = 0;
    const finalTotal = total + fee - discount;

    const handleOrder = () => {
        console.log(`Оформление заказа для пользователя ${userId}`);
        alert('Заказ оформлен!');
    };

    return (
        <div style={{ backgroundColor: '#666', padding: '20px', borderRadius: '8px' }}>
            <h2 style={{ color: '#fff', fontSize: '1.5rem', marginBottom: '15px' }}>Оплата</h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: '#ccc' }}>Итого:</span>
                    <span style={{ color: '#fff' }}>{total.toFixed(2)} ₽</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: '#ccc' }}>Пошлина:</span>
                    <span style={{ color: '#fff' }}>{fee.toFixed(2)} ₽</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: '#ccc' }}>Скидка:</span>
                    <span style={{ color: '#fff' }}>{discount.toFixed(2)} ₽</span>
                </div>
                <div style={{ borderTop: '1px solid #ccc', paddingTop: '10px', marginTop: '10px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 'bold' }}>
                        <span style={{ color: '#ccc' }}>К оплате:</span>
                        <span style={{ color: '#fff' }}>{finalTotal.toFixed(2)} ₽</span>
                    </div>
                </div>
                <button
                    onClick={handleOrder}
                    style={{
                        width: '100%',
                        padding: '10px',
                        backgroundColor: '#FFC107',
                        color: '#fff',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        marginTop: '15px',
                    }}
                >
                    Оформить
                </button>
            </div>
        </div>
    );
};

export default CartSummary;