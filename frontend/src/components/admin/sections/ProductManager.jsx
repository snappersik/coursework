import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { useDropzone } from 'react-dropzone';
import { toast } from 'react-toastify';
import { API_URL } from '../../../config';
import { FaPlus, FaTrashRestore, FaTrash, FaPen, FaLink, FaFilePdf, FaFileAudio, FaFileArchive, FaFileAlt } from 'react-icons/fa'; // Added more icons
import { FaSort, FaSortAlphaDown, FaSortAlphaUp } from 'react-icons/fa';


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
    category: 'E_BOOK', // Default category
    coverUrl: '', // For displaying existing or new URL for cover
    coverFile: null, // For new cover file upload
    electronicProductFilename: '', // For displaying existing e-product filename
    electronicFile: null, // For new e-product file upload
  });

  const { getRootProps: getCoverRootProps, getInputProps: getCoverInputProps, isDragActive: isCoverDragActive } = useDropzone({
    onDrop: useCallback(acceptedFiles => {
      const file = acceptedFiles[0];
      setFormData(prevForm => ({
        ...prevForm,
        coverFile: file,
        coverUrl: URL.createObjectURL(file), // Show preview for new file
      }));
    }, []),
    accept: { 'image/*': ['.jpg', '.jpeg', '.png', '.gif', '.webp'] },
    multiple: false,
  });

  const { getRootProps: getElectronicFileRootProps, getInputProps: getElectronicFileInputProps, isDragActive: isElectronicFileDragActive } = useDropzone({
    onDrop: useCallback(acceptedFiles => {
      const file = acceptedFiles[0];
      setFormData(prevForm => ({
        ...prevForm,
        electronicFile: file,
        // Display the name of the new file, not a URL preview for non-images
        electronicProductFilename: file ? file.name : (currentProduct?.electronicProductFilename || ''),
      }));
    }, [currentProduct]),
    // Adjust accept based on typical e-product types
    accept: { 
      'application/pdf': ['.pdf'],
      'audio/*': ['.mp3', '.wav', '.aac'],
      'application/zip': ['.zip'],
      'application/epub+zip': ['.epub'],
      'application/vnd.mobipocket-ebook': ['.mobi']
    }, 
    multiple: false,
  });

  useEffect(() => {
    fetchProducts();
    fetchCategories();
  }, []);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const { data } = await axios.get(`${API_URL}/products/search?page=0&size=100`, { withCredentials: true });
      setProducts(data.content || []);
    } catch (error) {
      toast.error('Не удалось загрузить товары');
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const { data } = await axios.get(`${API_URL}/products/categories/with-descriptions`, { withCredentials: true });
      setCategories(Array.isArray(data) ? data : []);
    } catch (error) {
      toast.error('Не удалось загрузить категории');
      setCategories([]);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleCoverUrlChange = (e) => {
    const url = e.target.value;
    setFormData(prev => ({
      ...prev,
      coverUrl: url,
      coverFile: null // Clear file if URL is manually set
    }));
  };
      
  const getElectronicFileIcon = (filename) => {
    if (!filename) return <FaFileAlt className="text-gray-400" />;
    const ext = filename.split('.').pop().toLowerCase();
    if (ext === 'pdf') return <FaFilePdf className="text-red-500" />;
    if (['mp3', 'wav', 'aac'].includes(ext)) return <FaFileAudio className="text-blue-500" />;
    if (['zip', 'rar'].includes(ext)) return <FaFileArchive className="text-yellow-500" />;
    return <FaFileAlt className="text-gray-400" />;
  };


  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = new FormData();
    const productDto = {
      name: formData.name,
      description: formData.description,
      price: parseFloat(formData.price) || 0,
      category: formData.category,
      coverImageUrl: formData.coverFile ? null : formData.coverUrl, // Send URL only if no new file
      // electronicProductFilename will be set by backend based on uploaded file
    };

    if (currentProduct) { // Include ID for updates
        productDto.id = currentProduct.id;
    }
        
    payload.append('product', new Blob([JSON.stringify(productDto)], { type: 'application/json' }));

    if (formData.coverFile) {
      payload.append('coverFile', formData.coverFile);
    }
    if (formData.electronicFile) {
      payload.append('electronicFile', formData.electronicFile);
    }

    try {
      if (currentProduct) {
        await axios.put(`${API_URL}/products/${currentProduct.id}`, payload, { withCredentials: true });
        toast.success('Товар успешно обновлен');
      } else {
        await axios.post(`${API_URL}/products`, payload, { withCredentials: true });
        toast.success('Товар успешно создан');
      }
      setShowModal(false);
      fetchProducts();
    } catch (error) {
      console.error('Ошибка сохранения товара:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Не удалось сохранить товар';
      toast.error(errorMessage);
    }
  };

  const handleEdit = (product) => {
    setCurrentProduct(product);
    setFormData({
      name: product.name || '',
      description: product.description || '',
      price: product.price || 0,
      category: product.category || 'E_BOOK',
      coverUrl: product.coverImageUrl || (product.coverImageFilename ? `${API_URL}/products/${product.id}/cover` : ''),
      coverFile: null,
      electronicProductFilename: product.originalElectronicProductFilename || '', // Display original name
      electronicFile: null,
    });
    setShowModal(true);
  };

  const handleSoftDelete = (id) => {
    // Logic for soft delete (e.g., API call and UI update)
    setSoftDeletedProducts(prev => ({ ...prev, [id]: true }));
    toast.warning('Товар помечен на удаление');
    // Example API call (adjust as needed):
    // axios.patch(`${API_URL}/products/${id}/soft-delete`, {}, { withCredentials: true })
    //   .then(() => fetchProducts())
    //   .catch(() => toast.error('Ошибка мягкого удаления'));
  };

  const handleRestore = (id) => {
    setSoftDeletedProducts(prev => {
      const newState = { ...prev };
      delete newState[id];
      return newState;
    });
    toast.success('Товар восстановлен (в UI)');
    // Example API call (adjust as needed):
    // axios.patch(`${API_URL}/products/${id}/restore`, {}, { withCredentials: true })
    //   .then(() => fetchProducts())
    //   .catch(() => toast.error('Ошибка восстановления'));
  };
      
  const handleDelete = async (id) => {
    if (window.confirm('Вы уверены, что хотите удалить этот товар? Это действие нельзя отменить.')) {
      try {
        await axios.delete(`${API_URL}/products/${id}`, { withCredentials: true });
        toast.success('Товар успешно удален');
        fetchProducts();
        setSoftDeletedProducts(prev => {
          const newState = { ...prev };
          delete newState[id];
          return newState;
        });
      } catch (error) {
        console.error('Ошибка удаления товара:', error);
        toast.error(error.response?.data?.message || 'Не удалось удалить товар');
      }
    }
  };

  const handleAddNew = () => {
    setCurrentProduct(null);
    setFormData({
      name: '',
      description: '',
      price: 0,
      category: categories.length > 0 ? categories[0].name : 'E_BOOK',
      coverUrl: '',
      coverFile: null,
      electronicProductFilename: '',
      electronicFile: null,
    });
    setShowModal(true);
  };
      
  const requestSort = (key) => {
    let direction = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  const getSortIcon = (key) => {
    if (sortConfig.key !== key) return <FaSort className="ml-1 inline" />;
    return sortConfig.direction === 'asc' ? <FaSortAlphaDown className="ml-1 inline text-yellow-400" /> : <FaSortAlphaUp className="ml-1 inline text-yellow-400" />;
  };

  const filteredAndSortedProducts = React.useMemo(() => {
    let filtered = products.filter(product =>
      product.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      product.category?.toLowerCase().includes(searchQuery.toLowerCase())
    );
    if (sortConfig.key) {
      filtered.sort((a, b) => {
        if (a[sortConfig.key] < b[sortConfig.key]) return sortConfig.direction === 'asc' ? -1 : 1;
        if (a[sortConfig.key] > b[sortConfig.key]) return sortConfig.direction === 'asc' ? 1 : -1;
        return 0;
      });
    }
    return filtered;
  }, [products, searchQuery, sortConfig]);

  if (loading) {
    return <div className="p-4 flex justify-center"><div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div></div>;
  }

  return (
    <div className="p-4">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-white">Управление товарами</h2>
        <button onClick={handleAddNew} className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600">Добавить товар</button>
      </div>
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
                <th className="px-4 py-2 cursor-pointer" onClick={() => requestSort('name')}>Название {getSortIcon('name')}</th>
                <th className="px-4 py-2 cursor-pointer" onClick={() => requestSort('category')}>Категория {getSortIcon('category')}</th>
                <th className="px-4 py-2 cursor-pointer" onClick={() => requestSort('price')}>Цена {getSortIcon('price')}</th>
                <th className="px-4 py-2">Эл. файл</th>
                <th className="px-4 py-2">Действия</th>
              </tr>
            </thead>
            <tbody>
              {filteredAndSortedProducts.map(product => (
                <tr key={product.id} className={`bg-[#585858] border-t border-gray-700 ${softDeletedProducts[product.id] ? 'opacity-60' : ''}`}>
                  <td className="px-4 py-2 text-left">{product.name}</td>
                  <td className="px-4 py-2">{product.category}</td>
                  <td className="px-4 py-2">{product.price?.toFixed(2)} руб.</td>
                  <td className="px-4 py-2">
                    {product.hasElectronicFile ? (
                        <span title={product.originalElectronicProductFilename || 'Электронный файл'} className="text-green-400">
                            {getElectronicFileIcon(product.originalElectronicProductFilename)} Да
                        </span>
                    ) : (
                        <span className="text-gray-400">Нет</span>
                    )}
                  </td>
                  <td className="px-4 py-2 flex justify-center space-x-2">
                    {!softDeletedProducts[product.id] ? (
                      <>
                        <button onClick={() => handleEdit(product)} className="p-2 bg-gray-600 text-white rounded hover:bg-gray-500" title="Редактировать"><FaPen /></button>
                        <button onClick={() => handleSoftDelete(product.id)} className="p-2 bg-yellow-500 text-white rounded hover:bg-yellow-600" title="Удалить"><FaTrash /></button>
                      </>
                    ) : (
                      <>
                        <button onClick={() => handleRestore(product.id)} className="p-2 bg-transparent border border-yellow-400 text-yellow-400 rounded hover:bg-yellow-400 hover:bg-opacity-10" title="Восстановить"><FaTrashRestore /></button>
                        <button onClick={() => handleDelete(product.id)} className="p-2 bg-red-600 text-white rounded hover:bg-red-700" title="Удалить навсегда"><FaTrash /></button>
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
          <p className="text-xl text-gray-300">Товары не найдены</p>
          <p className="text-gray-400 mt-2">Добавьте новый товар или измените параметры поиска</p>
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-[#585858] p-6 rounded-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-bold mb-4 text-white">{currentProduct ? 'Редактировать товар' : 'Добавить товар'}</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-300">Название</label>
                <input type="text" name="name" value={formData.name} onChange={handleInputChange} className="mt-1 block w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" required />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300">Описание</label>
                <textarea name="description" value={formData.description} onChange={handleInputChange} rows="3" className="mt-1 block w-full p-2 bg-[#707070] border border-gray-600 rounded text-white"></textarea>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-300">Цена (руб.)</label>
                  <input type="number" name="price" value={formData.price} onChange={handleInputChange} min="0" step="0.01" className="mt-1 block w-full p-2 bg-[#707070] border border-gray-600 rounded text-white" required />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300">Категория</label>
                  <select name="category" value={formData.category} onChange={handleInputChange} className="mt-1 block w-full p-2 bg-[#707070] border border-gray-600 rounded text-white">
                    {categories.map(cat => <option key={cat.name} value={cat.name}>{cat.description}</option>)}
                  </select>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300">URL обложки</label>
                <div className="flex">
                    <input type="text" name="coverUrlInput" value={formData.coverUrl} onChange={handleCoverUrlChange} placeholder="https://example.com/image.jpg" className="mt-1 flex-1 p-2 bg-[#707070] border border-gray-600 rounded-l text-white" />
                    <button type="button" className="mt-1 p-2 bg-blue-600 text-white rounded-r hover:bg-blue-700" onClick={() => { if (!formData.coverUrl) toast.error('Введите URL'); else toast.info('URL установлен');}}><FaLink /></button>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300">Загрузить обложку</label>
                <div {...getCoverRootProps()} className={`mt-1 border-2 border-dashed p-4 rounded text-center cursor-pointer ${isCoverDragActive ? 'border-yellow-500 bg-[#707070]' : 'border-gray-600'}`}>
                  <input {...getCoverInputProps()} />
                  {formData.coverFile ? (
                    <img src={URL.createObjectURL(formData.coverFile)} alt="Preview" className="h-20 mx-auto mb-1 object-contain" />
                  ) : formData.coverUrl && !formData.coverUrl.startsWith('blob:') ? (
                     <img src={formData.coverUrl} alt="Current Cover" className="h-20 mx-auto mb-1 object-contain" onError={(e) => e.target.style.display='none'}/>
                  ) : null}
                  <p className="text-white text-sm">{formData.coverFile ? formData.coverFile.name : (isCoverDragActive ? 'Отпустите файл...' : 'Перетащите файл или кликните')}</p>
                </div>
              </div>
                  
              {/* Electronic File Upload */}
              <div>
                <label className="block text-sm font-medium text-gray-300">Электронный файл (PDF, MP3, ZIP и т.д.)</label>
                <div {...getElectronicFileRootProps()} className={`mt-1 border-2 border-dashed p-4 rounded text-center cursor-pointer ${isElectronicFileDragActive ? 'border-yellow-500 bg-[#707070]' : 'border-gray-600'}`}>
                  <input {...getElectronicFileInputProps()} />
                  {formData.electronicFile ? (
                    <div className="flex items-center justify-center text-white">
                      {getElectronicFileIcon(formData.electronicFile.name)}
                      <span className="ml-2">{formData.electronicFile.name}</span>
                    </div>
                  ) : formData.electronicProductFilename ? (
                     <div className="flex items-center justify-center text-white">
                       {getElectronicFileIcon(formData.electronicProductFilename)}
                       <span className="ml-2">{formData.electronicProductFilename} (текущий)</span>
                     </div>
                  ) : (
                    <p className="text-white">{isElectronicFileDragActive ? 'Отпустите файл...' : 'Перетащите файл или кликните для выбора'}</p>
                  )}
                </div>
                {formData.electronicFile && <button type="button" onClick={() => setFormData(prev => ({...prev, electronicFile: null, electronicProductFilename: currentProduct?.originalElectronicProductFilename || ''}))} className="mt-1 text-xs text-red-400 hover:text-red-300">Удалить выбранный файл</button>}
              </div>

              <div className="flex justify-end space-x-2 pt-4">
                <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-500">Отмена</button>
                <button type="submit" className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600">{currentProduct ? 'Сохранить' : 'Добавить'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductManager;
