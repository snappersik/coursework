import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { API_URL } from '../../../config';
import { FaSort, FaSortAlphaDown, FaSortAlphaUp, FaTrashRestore, FaTrash, FaPen, FaPlus, FaLink } from 'react-icons/fa';
import { useDropzone } from 'react-dropzone';

const ProductManager = () => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [currentProduct, setCurrentProduct] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
  const [softDeletedProducts, setSoftDeletedProducts] = useState({});
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: 0,
    category: 'BOOK',
    coverUrl: '',
    coverFile: null
  });

  useEffect(() => {
    fetchProducts();
    fetchCategories();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/products/paginated`, { withCredentials: true });
      setProducts(response.data.content || []);
    } catch (error) {
      console.error('Ошибка загрузки продуктов:', error);
      toast.error('Не удалось загрузить продукты');
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await axios.get(`${API_URL}/products/categories/with-descriptions`, { withCredentials: true });
      setCategories(response.data || []);
    } catch (error) {
      console.error('Ошибка загрузки категорий:', error);
      toast.error('Не удалось загрузить категории');
    }
  };

  const onDrop = useCallback((acceptedFiles) => {
    const file = acceptedFiles[0];
    setFormData(prevForm => ({
      ...prevForm,
      coverFile: file,
      coverUrl: URL.createObjectURL(file),
    }));
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/*': ['.jpg', '.jpeg', '.png', '.gif', '.webp']
    }
  });

  const handleInputChange = (e) => {
    const { name, value } = e.target;

    if (name === 'price') {
      // Убедимся, что цена - положительное число
      const price = parseFloat(value);
      setFormData({
        ...formData,
        [name]: isNaN(price) ? 0 : Math.max(0, price)
      });
    } else {
      setFormData({
        ...formData,
        [name]: value
      });
    }
  };

  const handleCoverUrlChange = (e) => {
    const url = e.target.value;
    setFormData({
      ...formData,
      coverUrl: url,
      coverFile: null
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const productToSend = {
        name: formData.name,
        description: formData.description,
        price: formData.price,
        category: formData.category
      };

      const formDataToSend = new FormData();
      formDataToSend.append('product', new Blob([JSON.stringify(productToSend)], { type: 'application/json' }));

      if (formData.coverFile) {
        formDataToSend.append('file', formData.coverFile);
      }

      if (currentProduct) {
        // Обновление продукта
        await axios.put(`${API_URL}/products/${currentProduct.id}`, formDataToSend, {
          withCredentials: true
        });
        toast.success('Продукт успешно обновлен');
      } else {
        // Создание нового продукта
        await axios.post(`${API_URL}/products`, formDataToSend, {
          withCredentials: true
        });
        toast.success('Продукт успешно создан');
      }
      setShowModal(false);
      fetchProducts();
    } catch (error) {
      console.error('Ошибка сохранения продукта:', error);
      toast.error('Не удалось сохранить продукт');
    }
  };

  const handleEdit = (product) => {
    setCurrentProduct(product);
    setFormData({
      name: product.name || '',
      description: product.description || '',
      price: product.price || 0,
      category: product.category || 'BOOK',
      coverUrl: product.coverImageUrl || '',
      coverFile: null
    });
    setShowModal(true);
  };

  const handleSoftDelete = (id) => {
    setSoftDeletedProducts(prev => ({
      ...prev,
      [id]: true
    }));
    toast.warning('Продукт помечен на удаление');
  };

  const handleRestore = (id) => {
    setSoftDeletedProducts(prev => {
      const newState = { ...prev };
      delete newState[id];
      return newState;
    });
    toast.success('Продукт восстановлен');
  };

  const handleDelete = async (id) => {
    if (window.confirm('Вы уверены, что хотите удалить этот продукт? Это действие нельзя отменить.')) {
      try {
        await axios.delete(`${API_URL}/products/${id}`, { withCredentials: true });
        toast.success('Продукт успешно удален');
        fetchProducts();
        setSoftDeletedProducts(prev => {
          const newState = { ...prev };
          delete newState[id];
          return newState;
        });
      } catch (error) {
        console.error('Ошибка удаления продукта:', error);
        toast.error('Не удалось удалить продукт');
      }
    }
  };

  const handleAddNew = () => {
    setCurrentProduct(null);
    setFormData({
      name: '',
      description: '',
      price: 0,
      category: 'BOOK',
      coverUrl: '',
      coverFile: null
    });
    setShowModal(true);
  };

  // Функция сортировки
  const requestSort = (key) => {
    let direction = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  // Получение иконки сортировки
  const getSortIcon = (key) => {
    if (sortConfig.key !== key) {
      return <FaSort className="ml-1 inline" />;
    }
    return sortConfig.direction === 'asc'
      ? <FaSortAlphaDown className="ml-1 inline text-yellow-400" />
      : <FaSortAlphaUp className="ml-1 inline text-yellow-400" />;
  };

  // Получение отображаемого имени категории
  const getCategoryDisplayName = (categoryName) => {
    const category = categories.find(c => c.name === categoryName);
    return category ? category.description : categoryName;
  };

  // Фильтрация и сортировка продуктов
  const filteredAndSortedProducts = React.useMemo(() => {
    // Сначала фильтруем по поисковому запросу
    let filteredProducts = products.filter(product =>
      product.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      getCategoryDisplayName(product.category)?.toLowerCase().includes(searchQuery.toLowerCase())
    );

    // Затем сортируем
    if (sortConfig.key) {
      filteredProducts.sort((a, b) => {
        if (a[sortConfig.key] < b[sortConfig.key]) {
          return sortConfig.direction === 'asc' ? -1 : 1;
        }
        if (a[sortConfig.key] > b[sortConfig.key]) {
          return sortConfig.direction === 'asc' ? 1 : -1;
        }
        return 0;
      });
    }

    return filteredProducts;
  }, [products, searchQuery, sortConfig, categories]);

  if (loading) {
    return (
      <div className="p-4 flex justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
      </div>
    );
  }

  return (
    <div className="p-4">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-white">Управление продуктами</h2>
        <button
          onClick={handleAddNew}
          className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600 flex items-center"
        >
          <FaPlus className="mr-2" /> Добавить продукт
        </button>
      </div>

      {/* Поиск */}
      <div className="mb-4">
        <input
          type="text"
          placeholder="Поиск по названию или категории..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full p-2 bg-[#707070] border border-gray-600 rounded text-gray-200 placeholder-gray-400"
        />
      </div>

      {filteredAndSortedProducts.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="min-w-full text-white [&_th]:text-center [&_td:not(:first-child)]:text-center">
            <thead className="bg-[#626262]">
              <tr>
                <th
                  className="px-4 py-2 cursor-pointer"
                  onClick={() => requestSort('name')}
                >
                  Название {getSortIcon('name')}
                </th>
                <th
                  className="px-4 py-2 cursor-pointer"
                  onClick={() => requestSort('category')}
                >
                  Категория {getSortIcon('category')}
                </th>
                <th
                  className="px-4 py-2 cursor-pointer"
                  onClick={() => requestSort('price')}
                >
                  Цена {getSortIcon('price')}
                </th>
                <th className="px-4 py-2">Действия</th>
              </tr>
            </thead>
            <tbody>
              {filteredAndSortedProducts.map(product => (
                <tr
                  key={product.id}
                  className={`bg-[#585858] border-t border-gray-700 ${softDeletedProducts[product.id] ? 'opacity-60' : ''}`}
                >
                  <td className="px-4 py-2">{product.name}</td>
                  <td className="px-4 py-2">
                    <span className={`px-2 py-1 rounded ${product.category === 'BOOK' ? 'bg-blue-600' :
                        product.category === 'E_BOOK' ? 'bg-green-600' :
                          product.category === 'AUDIO_BOOK' ? 'bg-purple-600' :
                            product.category === 'MERCHANDISE' ? 'bg-orange-600' :
                              product.category === 'GIFT_CARD' ? 'bg-pink-600' :
                                'bg-gray-600'
                      }`}>
                      {getCategoryDisplayName(product.category)}
                    </span>
                  </td>
                  <td className="px-4 py-2">{product.price} ₽</td>
                  <td className="px-4 py-2 flex justify-center space-x-2">
                    {!softDeletedProducts[product.id] ? (
                      <>
                        <button
                          onClick={() => handleEdit(product)}
                          className="p-2 bg-gray-600 text-white rounded hover:bg-gray-500"
                          title="Редактировать"
                        >
                          <FaPen />
                        </button>
                        <button
                          onClick={() => handleSoftDelete(product.id)}
                          className="p-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
                          title="Удалить"
                        >
                          <FaTrash />
                        </button>
                      </>
                    ) : (
                      <>
                        <button
                          onClick={() => handleRestore(product.id)}
                          className="p-2 bg-transparent border border-yellow-400 text-yellow-400 rounded hover:bg-yellow-400 hover:bg-opacity-10"
                          title="Восстановить"
                        >
                          <FaTrashRestore />
                        </button>
                        <button
                          onClick={() => handleDelete(product.id)}
                          className="p-2 bg-red-600 text-white rounded hover:bg-red-700"
                          title="Удалить навсегда"
                        >
                          <FaTrash />
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="text-center py-10 bg-[#585858] rounded-lg">
          <p className="text-xl text-gray-300">Продукты не найдены</p>
          <p className="text-gray-400 mt-2">Добавьте новый продукт или измените параметры поиска</p>
        </div>
      )}

      {/* Модальное окно для добавления/редактирования продукта */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-[#585858] p-6 rounded-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-bold mb-4 text-white">{currentProduct ? 'Редактировать продукт' : 'Добавить продукт'}</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-300">Название</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  className="mt-1 block w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                  required
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-300">Категория</label>
                  <select
                    name="category"
                    value={formData.category}
                    onChange={handleInputChange}
                    className="mt-1 block w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                    required
                  >
                    {categories.map(category => (
                      <option key={category.name} value={category.name}>
                        {category.description}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-300">Цена (₽)</label>
                  <input
                    type="number"
                    name="price"
                    value={formData.price}
                    onChange={handleInputChange}
                    min="0"
                    step="0.01"
                    className="mt-1 block w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300">Описание</label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  rows="4"
                  className="mt-1 block w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"
                ></textarea>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300">URL обложки</label>
                <div className="flex">
                  <input
                    type="text"
                    name="coverUrlInput"
                    value={formData.coverUrl}
                    onChange={handleCoverUrlChange}
                    placeholder="https://example.com/image.jpg"
                    className="mt-1 flex-1 p-2 bg-[#707070] border border-gray-600 rounded-l text-white"
                  />
                  <button
                    type="button"
                    className="mt-1 p-2 bg-blue-600 text-white rounded-r hover:bg-blue-700"
                    onClick={() => {
                      if (!formData.coverUrl) {
                        toast.error('Введите URL изображения');
                        return;
                      }
                      toast.info('URL обложки установлен');
                    }}
                  >
                    <FaLink />
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300">Загрузить обложку</label>
                <div
                  {...getRootProps()}
                  className={`mt-1 border-2 border-dashed p-4 rounded text-center cursor-pointer ${isDragActive ? 'border-yellow-500 bg-[#707070]' : 'border-gray-600'
                    }`}
                >
                  <input {...getInputProps()} />
                  {formData.coverUrl && formData.coverFile ? (
                    <div className="flex flex-col items-center">
                      <img src={formData.coverUrl} alt="Preview" className="h-32 object-contain mb-2" />
                      <p className="text-white">Перетащите новый файл или кликните для замены</p>
                    </div>
                  ) : formData.coverUrl ? (
                    <div className="flex flex-col items-center">
                      <img src={formData.coverUrl} alt="Preview" className="h-32 object-contain mb-2" />
                      <p className="text-white">Используется URL изображения</p>
                    </div>
                  ) : (
                    <p className="text-white">Перетащите файл или кликните для выбора</p>
                  )}
                </div>
              </div>

              <div className="flex justify-end space-x-2 pt-4">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-500"
                >
                  Отмена
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
                >
                  {currentProduct ? 'Сохранить' : 'Добавить'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductManager;
