import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/layout/Header';

function App() {
  return (
    <Router>
      <div className="App">
        <Header />
        <main>
          <Routes>
            {/* <Route path="/" element={<Home />} /> */}
            {/* <Route path="/catalog" element={<Catalog />} /> */}
            {/* <Route path="/cart" element={<Cart />} /> */}
            {/* <Route path="/contact" element={<Contact />} /> */}
            {/* <Route path="/login" element={<Login />} /> */}
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;