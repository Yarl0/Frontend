# Proyecto CDD - Reconocimiento de Objetos

**Integrantes:**  
- Carlos Javier Abaunza  
- Jhan Carlo Leon  

Este proyecto implementa una API de reconocimiento de frutas y verduras utilizando un modelo YOLOv8 desplegado en un servidor EC2 de AWS. La API recibe imágenes desde una app móvil (Android Studio), identifica los ingredientes y sugiere recetas basadas en ellos.

---

## 🔧 Backend en AWS EC2

### 1. Creación y configuración de la instancia
- Se crea una nueva instancia en AWS EC2.
- Se accede a las reglas de entrada del grupo de seguridad y se añade:
  - Tipo: TCP personalizado  
  - Puerto: `8080`  
  - Origen: Anywhere - IPv4 (`0.0.0.0/0`)

### 2. Conexión al servidor
- Conectarse vía SSH utilizando la dirección IPv4 pública.
- Crear carpeta del proyecto y entorno virtual:

```bash
mkdir Proyecto
cd Proyecto/
python3 -m venv venv
source venv/bin/activate
```

### 3. Instalación de dependencias

```bash
pip install --upgrade pip
pip install fastapi uvicorn python-multipart requests
pip install ultralytics
```

> ⚠️ Si hay error por almacenamiento, limpiar caché:
```bash
rm -rf ~/.cache/pip
```

#### Problemas conocidos
- ⚠️ Si `torch.load` no funciona:

```bash
pip uninstall -y torch
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu
```

- ⚠️ Si hay conflictos con `numpy`:

```bash
pip uninstall numpy
pip install "numpy<2" --force-reinstall
```

### 4. Subir el modelo al servidor
Subir el modelo `.pt` entrenado desde tu PC al servidor EC2 mediante `scp` o alguna herramienta que soporte autenticación con archivo `.pem`.

---

## 🐍 API en FastAPI

Crear el archivo `app.py`:

```python
from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from PIL import Image
from ultralytics import YOLO
import io

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

model = YOLO("model/best.pt")

CLASES_VALIDAS = [
    "ajo", "apple", "banana", "cebolla", "champinon", "kiwi",
    "lechuga", "limon", "naranja", "pepino", "pera", "pimiento",
    "potato", "repollo", "tomate", "zanahoria"
]

def limpiar_duplicados(objetos):
    return sorted(list(set(objetos)))

def generar_receta(ingredientes):
    recetas = {
        "Ajo": "Salsa de ajo cremosa.",
        "Apple": "Ensalada de manzana con nueces.",
        "Banana": "Smoothie de banana y avena.",
        "Cebolla": "Cebolla caramelizada como guarnición.",
        "Champinon": "Salteado de champiñones al ajillo.",
        "Kiwi": "Ensalada tropical con kiwi y naranja.",
        "Lechuga": "Ensalada verde con limón.",
        "Limon": "Aderezo de limón natural.",
        "Naranja": "Jugo de naranja fresco.",
        "Pepino": "Ensalada de pepino y yogur.",
        "Pera": "Peras al vino.",
        "Pimiento": "Pimientos rellenos vegetarianos.",
        "Potato": "Papas al horno con especias.",
        "Repollo": "Ensalada de repollo rallado.",
        "Tomate": "Ensalada caprese con tomate y albahaca.",
        "Zanahoria": "Sopa cremosa de zanahoria.",
    }
    return [recetas.get(i, f"Receta para {i} no disponible.") for i in ingredientes]

@app.get("/")
def read_root():
    return {"message": "API de detección funcionando correctamente"}

@app.post("/detectar/")
async def detectar(file: UploadFile = File(...)):
    contents = await file.read()
    image = Image.open(io.BytesIO(contents)).convert("RGB")
    results = model(image)[0]
    nombres = model.names
    objetos_detectados = [nombres[int(cls)] for cls in results.boxes.cls]
    ingredientes = limpiar_duplicados([i for i in objetos_detectados if i.lower() in CLASES_VALIDAS])
    receta = generar_receta(ingredientes)
    return JSONResponse(content={
        "ingredientes_detectados": ingredientes,
        "receta_sugerida": receta
    })
```

---

## ▶️ Ejecución del servidor

```bash
source venv/bin/activate
uvicorn app:app --host 0.0.0.0 --port 8080 --reload
```

- Acceder desde navegador:  
  `http://<tu_ip_publica>:8080/docs`

---

## 🧪 Verificación con Postman

1. Crear una nueva solicitud `POST` a `http://<tu_ip>:8080/detectar/`.
2. Ir a la pestaña **Body** → seleccionar **form-data**.
3. Añadir un campo de tipo `file` con una imagen.
4. Enviar y visualizar la respuesta JSON con ingredientes detectados y receta sugerida.

---

## 📦 Tecnologías utilizadas

- **FastAPI** para la construcción de la API REST.
- **YOLOv8** como modelo de detección de objetos.
- **AWS EC2** para el despliegue del backend.
- **Postman** para pruebas.
- **Android Studio** (frontend móvil).

---


Este proyecto fue desarrollado como parte del Curso de Ciencia de Datos.

Evidencias de android studio
![Image](https://github.com/user-attachments/assets/a76b8347-0db6-432d-a3a1-24f673182293)
![image](https://github.com/user-attachments/assets/b085771c-dd93-4b2b-acda-396fd0203ba3)
![image](https://github.com/user-attachments/assets/da65377b-d99a-48b6-bca4-0c454bb6b82d)
![image](https://github.com/user-attachments/assets/9e301ffb-2627-4d63-8c0a-26804e0d8b9e)
![image](https://github.com/user-attachments/assets/583caa4a-d71f-4a7a-801c-0f2112279e84)

Varios erroes en el xml, y errores visuales con los text view, hizo dificil la tarea de formular o vizualizar las necesidades del codigo


