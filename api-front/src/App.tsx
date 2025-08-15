import React, { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import { LogOut, ShieldCheck, Users, Settings, KeyRound, Github, Mail } from "lucide-react";

// ---- Типы данных ----
type User = {
  id: string;
  username: string;
  email: string;
};

type ApiFetchOptions = {
  token?: string;
  method?: "GET" | "POST" | "PUT" | "DELETE" | "PATCH";
  body?: Record<string, any>;
  baseUrl: string;
};

type PersistedState = {
  baseUrl?: string;
  token?: string;
  user?: User | null;
  roleHint?: string;
};

type OAuthProvider = {
  id: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
};

// ---- Utility fetch wrapper ----
async function apiFetch<T = any>(path: string, options: ApiFetchOptions): Promise<T> {
  const { token, method = "GET", body, baseUrl } = options;
  const headers: HeadersInit = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = `Bearer ${token}`;
  
  const url = `${baseUrl.replace(/\/$/, "")}${path.startsWith("/") ? path : "/" + path}`;
  const requestOptions: RequestInit = {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  };

  const res = await fetch(url, requestOptions);
  let data: any = null;
  
  try { 
    data = await res.json(); 
  } catch {} // Ошибка парсинга игнорируется
  
  if (!res.ok) {
    const msg = data?.message || data?.error || `${res.status} ${res.statusText}`;
    throw new Error(msg);
  }
  
  return data as T;
}

// ---- Local storage helpers ----
const LS = {
  get k() { return "demo_auth"; },
  load(): PersistedState {
    try { 
      return JSON.parse(localStorage.getItem(this.k) || "{}"); 
    } catch { 
      return {}; 
    }
  },
  save(obj: PersistedState) { 
    localStorage.setItem(this.k, JSON.stringify(obj)); 
  },
  clear() { 
    localStorage.removeItem(this.k); 
  }
};

// ---- Пропсы компонентов ----
interface HeaderProps {
  isAuthed: boolean;
  user: User | null;
  onLogout: () => void;
}

interface SettingsCardProps {
  baseUrl: string;
  setBaseUrl: React.Dispatch<React.SetStateAction<string>>;
  roleHint: string;
  setRoleHint: React.Dispatch<React.SetStateAction<string>>;
}

interface AuthCardProps {
  baseUrl: string;
  onAuthed: (token: string, user: User | null) => void;
}

interface UserAreaProps {
  baseUrl: string;
  token: string;
  user: User | null;
  roleHint: string;
}

interface AdminAreaProps {
  baseUrl: string;
  token: string;
  user: User | null;
}

// ---- Main App ----
export default function App() {
  const persisted = useMemo<PersistedState>(() => LS.load(), []);
  const [baseUrl, setBaseUrl] = useState(persisted.baseUrl || "http://localhost:8080");
  const [token, setToken] = useState(persisted.token || "");
  const [currentUser, setCurrentUser] = useState<User | null>(persisted.user || null);
  const [roleHint, setRoleHint] = useState(persisted.roleHint || "");
  const isAuthed = !!token;

  useEffect(() => { 
    LS.save({ baseUrl, token, user: currentUser, roleHint }); 
  }, [baseUrl, token, currentUser, roleHint]);

  const logout = () => { 
    setToken(""); 
    setCurrentUser(null); 
  };

  return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-slate-50 to-slate-100 p-6">      <div className="max-w-5xl mx-auto grid gap-6">
        <Header isAuthed={isAuthed} user={currentUser} onLogout={logout} />
        <SettingsCard 
          baseUrl={baseUrl} 
          setBaseUrl={setBaseUrl} 
          roleHint={roleHint} 
          setRoleHint={setRoleHint} 
        />
        <Tabs defaultValue={isAuthed ? "me" : "auth"} className="w-full">
          <TabsList className="grid grid-cols-3 w-full">
            <TabsTrigger value="auth">Auth</TabsTrigger>
            <TabsTrigger value="me">Me</TabsTrigger>
            <TabsTrigger value="admin">Admin</TabsTrigger>
          </TabsList>
          
          <TabsContent value="auth" className="mt-4">
            <AuthCard 
              baseUrl={baseUrl} 
              onAuthed={(t, u) => {
                setToken(t); 
                setCurrentUser(u);
              }} 
            />
          </TabsContent>
          
          <TabsContent value="me" className="mt-4">
            <UserArea 
              baseUrl={baseUrl} 
              token={token} 
              user={currentUser} 
              roleHint={roleHint} 
            />
          </TabsContent>
          
          <TabsContent value="admin" className="mt-4">
            <AdminArea 
              baseUrl={baseUrl} 
              token={token} 
              user={currentUser} 
            />
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

function Header({ isAuthed, user, onLogout }: HeaderProps) {
  return (
    <div className="flex items-center justify-between">
      <h1 className="text-2xl font-semibold tracking-tight">SecureHighloadAPI — Frontend Demo</h1>
      <div className="flex items-center gap-3">
        {user && (
          <Badge variant="secondary" className="text-sm">
            {user.username} ({user.email})
          </Badge>
        )}
        {isAuthed && (
          <Button variant="outline" onClick={onLogout}>
            <LogOut className="w-4 h-4 mr-2" /> Logout
          </Button>
        )}
      </div>
    </div>
  );
}

function SettingsCard({ baseUrl, setBaseUrl, roleHint, setRoleHint }: SettingsCardProps) {
  return (
    <Card className="shadow-sm">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Settings className="w-5 h-5"/> Settings
        </CardTitle>
      </CardHeader>
      <CardContent className="grid gap-4">
        <div className="grid gap-2">
          <Label htmlFor="baseUrl">API Base URL</Label>
          <Input 
            id="baseUrl" 
            value={baseUrl} 
            onChange={e => setBaseUrl(e.target.value)} 
            placeholder="http://localhost:8080" 
          />
        </div>
        <div className="grid gap-2">
          <Label htmlFor="roleHint">Client-side role hint (optional)</Label>
          <Input 
            id="roleHint" 
            value={roleHint} 
            onChange={e => setRoleHint(e.target.value)} 
            placeholder="admin | user ..." 
          />
          <p className="text-xs text-muted-foreground">
            API в архиве не возвращает роли в JWT, поэтому здесь можно подсказать фронту вашу роль для демо (например, «admin»).
          </p>
        </div>
      </CardContent>
    </Card>
  );
}

function AuthCard({ baseUrl, onAuthed }: AuthCardProps) {
  const [tab, setTab] = useState<"signin" | "signup">("signin");
  
  return (
    <Card className="shadow-sm">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <KeyRound className="w-5 h-5"/> Authentication
        </CardTitle>
      </CardHeader>
      <CardContent className="grid gap-6">
        <div className="flex gap-2">
          <Button 
            variant={tab === "signin" ? "default" : "outline"} 
            onClick={() => setTab("signin")}
          >
            Sign in
          </Button>
          <Button 
            variant={tab === "signup" ? "default" : "outline"} 
            onClick={() => setTab("signup")}
          >
            Sign up
          </Button>
        </div>
        
        {tab === "signin" ? (
          <SigninForm baseUrl={baseUrl} onAuthed={onAuthed} />
        ) : (
          <SignupForm baseUrl={baseUrl} onAuthed={onAuthed} />
        )}
        
        <Separator />
        <OAuthButtons baseUrl={baseUrl} />
      </CardContent>
    </Card>
  );
}

interface SigninFormProps {
  baseUrl: string;
  onAuthed: (token: string, user: User | null) => void;
}

function SigninForm({ baseUrl, onAuthed }: SigninFormProps) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true); 
    setError("");
    
    try {
      const res = await apiFetch<{ 
        token?: string; 
        access_token?: string;
        user?: User;
        data?: { token: string; user: User };
      }>("/auth/signin", { 
        baseUrl, 
        method: "POST", 
        body: { username, password } 
      });
      
      const token = res?.data?.token || res?.token || res?.access_token;
      const user = res?.data?.user || res?.user || null;
      
      if (!token) throw new Error("No token in response");
      onAuthed(token, user);
    } catch (err: any) { 
      setError(err.message || "Authentication failed"); 
    } finally { 
      setLoading(false); 
    }
  };

  return (
    <form className="grid gap-3" onSubmit={submit}>
      <div className="grid gap-2">
        <Label>Username</Label>
        <Input 
          value={username} 
          onChange={e => setUsername(e.target.value)} 
          required 
        />
      </div>
      <div className="grid gap-2">
        <Label>Password</Label>
        <Input 
          type="password" 
          value={password} 
          onChange={e => setPassword(e.target.value)} 
          required 
        />
      </div>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <Button type="submit" disabled={loading}>
        {loading ? "Signing in..." : "Sign in"}
      </Button>
    </form>
  );
}

function SignupForm({ baseUrl, onAuthed }: SigninFormProps) {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password.length < 6) { 
      setError("Password must be ≥ 6 chars (API rule)"); 
      return; 
    }
    
    setLoading(true); 
    setError("");
    
    try {
      // Регистрация
      await apiFetch("/auth/signup/save", { 
        baseUrl, 
        method: "POST", 
        body: { username, password, email } 
      });
      
      // Автоматический вход после регистрации
      const login = await apiFetch<{ 
        token?: string; 
        access_token?: string;
        user?: User;
        data?: { token: string; user: User };
      }>("/auth/signin", { 
        baseUrl, 
        method: "POST", 
        body: { username, password } 
      });
      
      const token = login?.data?.token || login?.token || login?.access_token;
      const user = login?.data?.user || login?.user || null;
      
      if (!token) throw new Error("No token in response");
      onAuthed(token, user);
    } catch (err: any) { 
      setError(err.message || "Registration failed"); 
    } finally { 
      setLoading(false); 
    }
  };

  return (
    <form className="grid gap-3" onSubmit={submit}>
      <div className="grid gap-2">
        <Label>Username</Label>
        <Input 
          value={username} 
          onChange={e => setUsername(e.target.value)} 
          required 
        />
      </div>
      <div className="grid gap-2">
        <Label>Email</Label>
        <Input 
          type="email" 
          value={email} 
          onChange={e => setEmail(e.target.value)} 
          required 
        />
      </div>
      <div className="grid gap-2">
        <Label>Password (≥16 chars)</Label>
        <Input 
          type="password" 
          value={password} 
          onChange={e => setPassword(e.target.value)} 
          required 
        />
      </div>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <Button type="submit" disabled={loading}>
        {loading ? "Signing up..." : "Sign up"}
      </Button>
    </form>
  );
}

interface OAuthButtonsProps {
  baseUrl: string;
}

function OAuthButtons({ baseUrl }: OAuthButtonsProps) {
  const providers: OAuthProvider[] = [
    { id: "google", label: "Continue with Google", icon: Mail },
    { id: "github", label: "Continue with GitHub", icon: Github },
  ];
  
  return (
    <div className="grid gap-2">
      {providers.map(p => (
        <Button 
          key={p.id} 
          variant="outline" 
          className="justify-start" 
          onClick={() => {
            const url = `${baseUrl.replace(/\/$/, "")}/oauth2/authorization/${p.id}`;
            window.location.href = url;
          }}
        >
          <p.icon className="w-4 h-4 mr-2" /> 
          {p.label}
        </Button>
      ))}
      <p className="text-xs text-muted-foreground">
        Это просто место под OAuth2: ожидаются стандартные Spring Security маршруты 
        <code>/oauth2/authorization/google</code> и <code>/oauth2/authorization/github</code>.
      </p>
    </div>
  );
}

function UserArea({ baseUrl, token, user, roleHint }: UserAreaProps) {
  const [securedEcho, setSecuredEcho] = useState("");
  const [error, setError] = useState("");

  const load = async () => {
    setError(""); 
    setSecuredEcho("");
    
    try {
      const res = await apiFetch<string | { data?: any }>(
        "/secured/user", 
        { baseUrl, token }
      );
      
      const text = typeof res === "string" 
        ? res 
        : (res?.data || JSON.stringify(res));
      
      setSecuredEcho(String(text));
    } catch (e: any) { 
      setError(e.message || "Failed to load secured data"); 
    }
  };

  useEffect(() => { 
    if (token) load(); 
  }, [token, baseUrl]);

  const effectiveRole = (roleHint || "").toLowerCase() || 
    (user?.username?.toLowerCase() === "admin" ? "admin" : "user");

  return (
    <Card className="shadow-sm">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <ShieldCheck className="w-5 h-5"/> Me & Secured API
        </CardTitle>
      </CardHeader>
      <CardContent className="grid gap-4">
        <div className="flex items-center gap-2">
          <Badge>{effectiveRole}</Badge>
          {user && <Badge variant="secondary">id: {user.id}</Badge>}
        </div>
        <div className="grid gap-2">
          <Label>GET /secured/user response</Label>
          <div className="p-3 rounded-xl border bg-white text-sm font-mono whitespace-pre-wrap min-h-[44px]">
            {securedEcho || "—"}
          </div>
          {error && <p className="text-sm text-red-600">{error}</p>}
        </div>
        <Separator />
        <UserDemoFeature />
      </CardContent>
    </Card>
  );
}

interface UserDemoFeatureProps {
  baseUrl?: string;
  token?: string;
  role?: string;
}

function UserDemoFeature({}: UserDemoFeatureProps) {
  const [note, setNote] = useState(localStorage.getItem("demo_note") || "");
  
  useEffect(() => { 
    localStorage.setItem("demo_note", note); 
  }, [note]);
  
  return (
    <div className="grid gap-2">
      <Label>User demo feature</Label>
      <p className="text-sm text-muted-foreground">
        Пример «фичи для юзера»: локальная заметка. Никаких прав не требуется.
      </p>
      <Input 
        value={note} 
        onChange={e => setNote(e.target.value)} 
        placeholder="type something…" 
      />
    </div>
  );
}

function AdminArea({ baseUrl, token }: AdminAreaProps) {
  const [users, setUsers] = useState<any[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const fetchUsers = async () => {
    setLoading(true); 
    setError("");
    
    try {
      const candidates = ["/users", "/api/users", "/admin/users"];
      let result: any[] | null = null;
      
      for (const path of candidates) {
        try {
          const res = await apiFetch<any>(path, { baseUrl, token });
          result = Array.isArray(res)
            ? res
            : res?.data || res?.users || [];
          
          if (Array.isArray(result)) break;
        } catch {}
      }
      
      if (!Array.isArray(result)) {
        throw new Error("No users endpoint found (tried /users, /api/users, /admin/users)");
      }
      
      setUsers(result);
    } catch (e: any) { 
      setError(e.message || "Failed to load users"); 
    } finally { 
      setLoading(false); 
    }
  };

  return (
    <Card className="shadow-sm">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Users className="w-5 h-5"/> Admin — Users
        </CardTitle>
      </CardHeader>
      <CardContent className="grid gap-4">
        <div className="flex items-center gap-2">
          <Button 
            onClick={fetchUsers} 
            disabled={!token || loading}
          >
            {loading ? "Loading..." : "Load users"}
          </Button>
          {!token && <p className="text-sm text-muted-foreground">Нужен токен. Войдите в систему.</p>}
        </div>
        
        {error && <p className="text-sm text-red-600">{error}</p>}
        
        <div className="overflow-auto rounded-xl border bg-white">
          <table className="min-w-full text-sm">
            <thead>
              <tr className="border-b bg-slate-50">
                <th className="text-left p-2">#</th>
                <th className="text-left p-2">ID</th>
                <th className="text-left p-2">Username</th>
                <th className="text-left p-2">Email</th>
              </tr>
            </thead>
            <tbody>
              {users.length > 0 ? users.map((u, i) => (
                <tr key={u.id ?? i} className="border-b">
                  <td className="p-2">{i + 1}</td>
                  <td className="p-2">{u.id ?? "—"}</td>
                  <td className="p-2">{u.username ?? "—"}</td>
                  <td className="p-2">{u.email ?? "—"}</td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={4} className="p-4 text-center text-muted-foreground">
                    No data
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        
        <p className="text-xs text-muted-foreground">
          Если сервер пока не отдает список пользователей, можно быстро добавить REST-эндпоинт на бэке 
          (GET /users) и вернуть <code>List&lt;UserResponse&gt;</code>. Фронт подхватит автоматически.
        </p>
      </CardContent>
    </Card>
  );
}