import React from 'react';

const CartItem = ({ item, userId }) => {
    // Заглушка: действия корзины пока не перенесены в MobX или API
    const handleRemove = () => {
        console.log(`Удаление товара ${item.id} для пользователя ${userId}`);
    };

    const handleQuantityChange = (newQuantity) => {
        if (newQuantity >= 0) {
            console.log(`Обновление количества товара ${item.id} на ${newQuantity} для пользователя ${userId}`);
        }
    };

    return (
        <div
            style={{
                display: 'flex',
                alignItems: 'center',
                padding: '10px',
                borderBottom: '1px solid #ddd',
                backgroundColor: '#666',
                borderRadius: '8px',
                marginBottom: '10px',
            }}
        >
            <img
                src={item.coverImageUrl || 'placeholder.jpg'}
                alt={item.productName}
                style={{ width: '100px', height: 'auto', marginRight: '15px', borderRadius: '4px' }}
            />
            <div style={{ flex: 1 }}>
                <h4 style={{ margin: 0, color: '#fff' }}>{item.productName}</h4>
                <p style={{ margin: '5px 0', color: '#ccc' }}>{item.price} ₽ за единицу</p>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                    <button
                        onClick={() => handleQuantityChange(item.quantity - 1)}
                        style={{
                            padding: '5px',
                            width: '25px',
                            height: '25px',
                            borderRadius: '50%',
                            backgroundColor: '#fff',
                            color: '#666',
                        }}
                    >
                        -
                    </button>
                    <input
                        type="number"
                        value={item.quantity}
                        onChange={(e) => handleQuantityChange(parseInt(e.target.value) || 0)}
                        style={{ width: '50px', margin: '0 10px', textAlign: 'center', backgroundColor: '#fff', color: '#000' }}
                    />
                    <button
                        onClick={() => handleQuantityChange(item.quantity + 1)}
                        style={{
                            padding: '5px',
                            width: '25px',
                            height: '25px',
                            borderRadius: '50%',
                            backgroundColor: '#fff',
                            color: '#666',
                        }}
                    >
                        +
                    </button>
                    <p style={{ marginLeft: '15px', color: '#fff' }}>Итого: {item.price * item.quantity} ₽</p>
                </div>
            </div>
            <button
                onClick={handleRemove}
                style={{
                    padding: '5px 10px',
                    backgroundColor: '#ff6347',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                }}
            >
                Удалить
            </button>
        </div>
    );
};

export default CartItem;